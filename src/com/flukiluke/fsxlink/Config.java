package com.flukiluke.fsxlink;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {
    // Literals used in the configuration
    public static final String SIMCONNECT = "simConnect";
    public static final String APPNAME = "appName";
    public static final String FAKE = "fake";
    public static final String IP = "ip";
    public static final String PORT = "port";

    public static final String SERIAL = "serial";
    public static final String DEVICE = "device";
    public static final String BAUD = "baud";
    public static final String ECHO = "echo";
    public static final String CONSOLE = "console";

    public static final String MAPPINGS = "mappings";
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String FOR = "for";
    public static final String CODE = "code";
    public static final String UNIT = "unit";
    public static final String TYPE = "type";
    public static final String TOGGLE = "toggle";
    public static final String PROBE = "probe";
    public static final String ROUND = "round";

    private static Config config;
    private Map data;
    public static final String CONFIG_FILE = "config.yml";

    public static void loadConfigFile() throws IOException {
        InputStream configStream;
        try {
            configStream = new FileInputStream(CONFIG_FILE);
        }
        catch (FileNotFoundException e) {
            configStream = makeConfigFile();
        }
        Yaml yaml = new Yaml(); //new Yaml(new CustomClassLoaderConstructor(Config.class.getClassLoader()));
        config = new Config(yaml.load(configStream));
        configStream.close();
    }

    private static InputStream makeConfigFile() throws IOException {
        InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        try {
            Files.copy(inputStream, Paths.get(CONFIG_FILE));
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            inputStream.close();
        }
        return Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
    }

    public static Config getConfig() {
        return config;
    }

    public Config getMap(String name) {
        return new Config((Map)data.get(name));
    }

    public List<Config> getListOfMaps(String name) {
        List<Config> list = new ArrayList<>();
        for (Map m : (List<Map>)data.get(name)) {
            list.add(new Config(m));
        }
        return list;
    }

    public List<String> getUnilistOfStrings(String name) {
        Object o = data.get(name);
        if (o instanceof List) {
            return (List<String>)o;
        }
        else {
            List<String> l = new ArrayList<>(1);
            if (o != null) {
                l.add((String) o);
            }
            return l;
        }
    }

    public String getString(String name) {
        return (String)data.get(name);
    }

    public String getString(String name, String otherwise) {
        if (data.containsKey(name)) {
            return (String) data.get(name);
        }
        return otherwise;
    }

    public Integer getInteger(String name) {
        return (Integer)data.get(name);
    }

    public Integer getInteger(String name, Integer otherwise) {
        if (data.containsKey(name)) {
            return (Integer) data.get(name);
        }
        return otherwise;
    }

    public Boolean getBoolean(String name, boolean otherwise) {
        if (data.containsKey(name)) {
            return (Boolean)data.get(name);
        }
        return otherwise;
    }

    private Config(Map data) {
        this.data = data;
    }
}

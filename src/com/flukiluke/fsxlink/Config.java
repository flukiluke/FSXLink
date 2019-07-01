package com.flukiluke.fsxlink;

import flightsim.simconnect.config.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Config {
    private static JSONObject config;

    public static void loadConfigFile(String filename) {
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader fileReader = new FileReader(filename);
            config = (JSONObject)jsonParser.parse(fileReader);
        }
        catch (FileNotFoundException e) {
            System.err.println("Config file " + filename + " not found");
            System.exit(1);
        }
        catch (IOException|ParseException e) {
            System.err.println("Error parsing " + filename + ": " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public static Configuration getSimConnectConfig() {
        Configuration scConfig = new Configuration();
        JSONObject jsonSc = (JSONObject)config.get("simconnect");
        scConfig.put("appName", (String)jsonSc.get("app_name"));
        scConfig.setAddress((String)jsonSc.get("ip"));
        scConfig.setPort((Integer)jsonSc.get("port"));
        scConfig.setProtocol((Integer)jsonSc.get("ip_version"));
        scConfig.put("simConnectProtocol", (String)jsonSc.get("protocol"));
        return scConfig;
    }

    public static List<Mapping> getInputMappings() {
        return getMapping("input_map");
    }

    public static List<Mapping> getOutputMappings() {
        return getMapping("output_map");
    }

    private static List<Mapping> getMapping(String fieldName) {
        List<Mapping> mappings = new ArrayList<>();
        JSONArray array = (JSONArray)config.get(fieldName);
        for (Object o : array) {
            JSONObject jsonMapping = (JSONObject)o;
            mappings.add(new Mapping(
                    (String)jsonMapping.get("name"),
                    (String)jsonMapping.get("command"),
                    (Long)jsonMapping.get("arg_length")
                    ));
        }
        return mappings;
    }

}

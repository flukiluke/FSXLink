package com.flukiluke.fsxlink;

import java.io.IOException;

public class FSXLink {
    public static final String CONFIG_FILE = "config.yml";

    private static SerialDevice serialDevice;
    private static Simulation simulation;

    public static void main(String[] args) {
        try {
            Config.loadConfigFile(CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Problem loading config file: " + e.getLocalizedMessage());
            System.exit(1);
        }

        SerialManager serialManager = new SerialManager();
        serialManager.probePorts();

        if (Config.getConfig().getMap(Config.SIMCONNECT).getBoolean(Config.FAKE, false)) {
            simulation = new NullSimulation();
        }
        else {
            try {
                simulation = new FSXSimulation();
            } catch (IOException e) {
                System.err.println("Problem connecting to Flight Simulator: " + e.getLocalizedMessage());
                System.exit(1);
            }
        }
        try {
            registerMappings();
            simulation.startDataHandler(serialDevice);
            mainLoop();
        } catch (IOException e) {
            System.err.println("Communication failed: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    private static void registerMappings() throws IOException {
        for (Config c : Config.getConfig().getListOfMaps(Config.MAPPINGS)) {
            Mapping m = new Mapping(c);
            if (m.isInput()) {
                serialDevice.registerInputMapping(m);
                simulation.registerInputMapping(m);
            }
            if (m.isOutput()) {
                simulation.registerOutputMapping(m);
            }
        }
    }

    private static void mainLoop() throws IOException {
        Command command;
        while (true) {
            command = serialDevice.readCommand();
            if (command == null) {
                continue;
            }
            simulation.sendEvent(command);
        }
    }
}
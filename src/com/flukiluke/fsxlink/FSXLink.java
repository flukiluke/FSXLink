package com.flukiluke.fsxlink;

import java.io.IOException;

public class FSXLink {
    public static final String CONFIG_FILE = "config.yml";

    private static SerialConnector serialConnector;
    private static SimulationConnector simulationConnector;

    public static void main(String[] args) {
        try {
            Config.loadConfigFile(CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Problem loading config file: " + e.getLocalizedMessage());
            System.exit(1);
        }
        try {
            serialConnector = new SerialConnector();
        } catch (IOException e) {
            System.err.println("Serial device error: " + e.getLocalizedMessage());
            System.exit(1);
        }
        try {
            simulationConnector = new SimulationConnector();
        } catch (IOException e) {
            System.err.println("Problem connecting to Flight Simulator: " + e.getLocalizedMessage());
            System.exit(1);
        }
        try {
            registerMappings();
            simulationConnector.startDataHandler(serialConnector);
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
                serialConnector.registerInputMapping(m);
                simulationConnector.registerInputMapping(m);
            }
            if (m.isOutput()) {
                simulationConnector.registerOutputMapping(m);
            }
        }
    }

    private static void mainLoop() throws IOException {
        Command command;
        while (true) {
            command = serialConnector.readCommand();
            System.out.println(command);
            simulationConnector.sendEvent(command);
        }
    }
}
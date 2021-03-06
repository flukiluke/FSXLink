package com.flukiluke.fsxlink;

import java.io.IOException;
import java.util.List;

public class FSXLink {

    private static SerialManager serialManager;
    private static Simulation simulation;

    public static void main(String[] args) {
        try {
            Config.loadConfigFile();
        } catch (IOException e) {
            System.err.println("Problem loading config file: " + e.getLocalizedMessage());
            System.exit(1);
        }

        serialManager = new SerialManager(Config.getConfig().getMap(Config.SERIAL));

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
            simulation.startDataHandler(serialManager);
            System.err.println("Ready");
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
                serialManager.registerInputMapping(m);
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
            command = serialManager.readCommand();
            if (command == null) {
                continue;
            }
            simulation.sendEvent(command);
        }
    }
}
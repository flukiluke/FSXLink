package com.flukiluke.fsxlink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Cli {
    private static Simulation simulation;

    public static void main(String[] args) throws IOException {
        try {
            Config.loadConfigFile(FSXLink.CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Problem loading config file: " + e.getLocalizedMessage());
            System.exit(1);
        }
        simulation = new FSXSimulation();
        Scanner inputScanner = new Scanner(System.in);
        while (true) {
            String[] parts = inputScanner.nextLine().split(" ");
            if (parts.length == 0)
                continue;
            switch (parts[0]) {
                case "send":
                    if (parts.length == 2) {
                        sendEvent(parts[1]);
                    }
                    else {
                        sendEvent(parts[1], Integer.parseInt(parts[2]));
                    }
                    break;
                case "watchint":
                    receiveInt(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1)),
                            parts[parts.length - 1], new ContinuousCommandPrinter());
                    break;
                case "watchfloat":
                    receiveFloat(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1)),
                            parts[parts.length - 1], new ContinuousCommandPrinter());
                    break;
                default:
                    System.err.println("send <event> <value>\n" +
                            "watchint <variable> <unit>\n" +
                            "watchfloat <variable> <unit>");
                    break;
            }
        }
    }

    private static void sendEvent(String eventName) throws IOException {
        sendEvent(eventName, 0);
    }

    private static void sendEvent(String eventName, Integer argument) throws IOException {
        List<String> inputs = new ArrayList<>();
        inputs.add(eventName);
        Mapping m = new Mapping(inputs, null, new ArrayList<String>(), null, null, "int", null);
        simulation.registerInputMapping(m);
        Command c = new Command(m, argument);
        simulation.sendEvent(c);
    }

    private static void receiveFloat(String variableName, String unit,  CommandHandler receiver) throws IOException {
        receiveData(variableName, unit, receiver, "float");
    }

    private static void receiveInt(String variableName, String unit,  CommandHandler receiver) throws IOException {
        receiveData(variableName, unit, receiver, "int");
    }

    private static void receiveData(String variableName, String unit,  CommandHandler receiver, String type) throws IOException {
        Mapping m = new Mapping(null, variableName, new ArrayList<String>(), null, unit, type, null);
        simulation.registerOutputMapping(m);
        simulation.startDataHandler(receiver);
    }

    private static class ContinuousCommandPrinter implements CommandHandler {
        @Override
        public void handleCommand(Command command) {
            System.out.println(command.mapping.outputName + ": " + command.argument);
        }
    }
}

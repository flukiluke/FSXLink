package com.flukiluke.fsxlink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        switch (args[0]) {
            case "send":
                if (args.length == 2) {
                    sendEvent(args[1]);
                }
                else {
                    sendEvent(args[1], Integer.parseInt(args[2]));
                }
                break;
            case "receive":
                if (args.length == 2) {
                    receiveData(args[1]);
                }
                else {
                    receiveData(args[1], args[2]);
                }
                break;
            default:
                System.err.println("Unknown command " + args[0]);
                showHelp();
        }
    }

    private static void sendEvent(String eventName) throws IOException {
        sendEvent(eventName, 0);
    }

    private static void sendEvent(String eventName, Integer argument) throws IOException {
        List<String> inputs = new ArrayList<>();
        inputs.add(eventName);
        Mapping m = new Mapping(inputs, null, null, null);
        simulation.registerInputMapping(m);
        Command c = new Command(m, argument);
        simulation.sendEvent(c);
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            // Ignore
        }
    }

    private static void receiveData(String variableName) throws IOException {
        receiveData(variableName, "");
    }

    private static void receiveData(String variableName, String unit) throws IOException {
        Mapping m = new Mapping(null, variableName, null, unit);
        simulation.registerOutputMapping(m);
        simulation.startDataHandler(new OneshotCommandPrinter());
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // Ignore
        }
    }

    private static void showHelp() {
        System.err.println("Tool for testing Flight Simulator inputs and outputs.\n\n"
                         + "Usage: " + Cli.class.getName() + " send <event name> [<argument>]\n"
                         + "       " + Cli.class.getName() + " receive <variable name> [<unit>]");
        System.exit(0);
    }

    private static class OneshotCommandPrinter implements CommandHandler {
        @Override
        public void handleCommand(Command command) {
            System.out.println(command.argument);
            System.exit(0);
        }
    }
}

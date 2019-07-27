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

        switch (args[1]) {
            case "send":
                if (args.length == 3) {
                    sendEvent(args[2]);
                }
                else {
                    sendEvent(args[2], Integer.parseInt(args[3]));
                }
                break;
            case "receive":
                if (args.length == 3) {
                    receiveData(args[2]);
                }
                else {
                    receiveData(args[2], args[3]);
                }
                break;
            default:
                System.err.println("Unknown command " + args[1]);
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
    }

    private static void receiveData(String variableName) throws IOException {
        receiveData(variableName, "");
    }

    private static void receiveData(String variableName, String unit) throws IOException {
        Mapping m = new Mapping(null, variableName, null, unit);
        simulation.registerOutputMapping(m);
        simulation.startDataHandler(new OneshotCommandPrinter());
        while (true) {
            try {
                Cli.class.wait();
            }
            catch (InterruptedException e) {
                // ignore
            }
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
            System.out.println(command);
            System.exit(0);
        }
    }
}

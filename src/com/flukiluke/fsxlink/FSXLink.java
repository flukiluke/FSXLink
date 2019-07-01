package com.flukiluke.fsxlink;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class FSXLink {
    public static void main(String[] args) throws IOException {
        Config.loadConfigFile("config.json");
        List<Mapping> inputMappings = Config.getInputMappings();

        SimulationConnector simulation = new SimulationConnector();
        for (Mapping m : inputMappings) {
            simulation.registerInputMapping(m);
        }

        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            String serialInput = input.nextLine();
            for (Mapping m : inputMappings) {
                if (serialInput.startsWith(m.serialCommand)) {
                    int argument = 0;
                    if (m.argLength > 0) {
                        argument = Integer.parseInt(serialInput.substring(m.serialCommand.length(), m.serialCommand.length() + m.argLength));
                    }
                    System.out.println(m.simconnectName);
                    System.out.println(argument);
                    // simulation.sendEvent(m, argument);
                    break;
                }
            }
        }
    }
}

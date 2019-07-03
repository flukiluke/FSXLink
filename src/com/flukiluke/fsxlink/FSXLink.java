package com.flukiluke.fsxlink;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class FSXLink {
    public static void main(String[] args) throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
        Config.loadConfigFile("config.json");
        List<Mapping> inputMappings = Config.getInputMappings();
        List<Mapping> outputMappings = Config.getOutputMappings();

        SimulationConnector simulation = new SimulationConnector();

        for (Mapping m : inputMappings) {
            simulation.registerInputMapping(m);
        }
        for (Mapping m : outputMappings) {
            simulation.registerOutputMapping(m);
        }

        SerialPort serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(Config.getSerialConfig().get("device")).open("sdf", 3);
        serialPort.setSerialPortParams(Integer.parseInt(Config.getSerialConfig().get("baud")),
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        InputStream input = serialPort.getInputStream();
        OutputStream output = serialPort.getOutputStream();

        String receivedCommand = "";
        while (true) {
            int i = input.read();
            if (i == -1) {
                System.out.println("Serial port closed");
                break;
            }
            // Echo - useful when the serial device is in fact a human
            // output.write((char)i);

            receivedCommand += (char)i;
            for (Mapping m : inputMappings) {
                if (receivedCommand.startsWith(m.serialCommand)) {
                    int argument = 0;
                    if (m.argLength > 0) {
                        byte[] receivedArgument = new byte[(int) m.argLength];
                        int bytesRead = 0;
                        while (bytesRead < m.argLength) {
                            bytesRead += input.read(receivedArgument, bytesRead, (int) m.argLength - bytesRead);
                        }
                        output.write(receivedArgument);
                        argument = Integer.parseInt(new String(receivedArgument));
                    }
                    System.out.println("        " + receivedCommand + argument);
                    simulation.sendEvent(m, argument);
                    receivedCommand = "";
                    break;
                }
            }
        }
        serialPort.close();
    }
}

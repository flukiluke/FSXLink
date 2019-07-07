package com.flukiluke.fsxlink;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialConnector implements DataCommandSink {
    private InputStream input;
    private OutputStream output;
    private PrefixTree<Mapping> mappings = new PrefixTree<>();
    private boolean echo;

    public SerialConnector() throws IOException {
        Config serialConfig = Config.getConfig().getMap(Config.SERIAL);
        if (serialConfig.getString(Config.DEVICE).equals("console")) {
            input = System.in;
            output = System.out;
            return;
        }
        echo = serialConfig.getBoolean(Config.ECHO);
        try {
            SerialPort serialPort = (SerialPort) CommPortIdentifier
                    .getPortIdentifier(serialConfig.getString(Config.DEVICE))
                    .open(getClass().getName(), 3);
            serialPort.setSerialPortParams(serialConfig.getInteger(Config.BAUD),
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
        }
        catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException("Connection to serial device at " + serialConfig.getString(Config.DEVICE)
                                    + " failed: " + e.toString());
        }
    }

    public void registerInputMapping(Mapping m) {
        mappings.add(m.code, m);
    }

    public void sendCommand(Command command) {
        try {
            output.write(command.toString().getBytes());
        }
        catch (IOException e) {
            System.err.println("Serial communications error");
            System.exit(1);
        }
    }

    public Command readCommand() throws IOException {
        String buffer = "";
        while (true) {
            char c = readCharBlocking();
            if (!mappings.isValidPrefix(buffer + c)) {
                System.err.println("Warning: received junk from serial device: " + buffer + c);
                buffer = "";
            }
            buffer += c;

            Mapping m = mappings.get(buffer);
            if (m != null) {
                if (m.digits == 0) {
                    return new Command(m);
                } else {
                    Integer argument = readArgument(m.digits);
                    if (argument == null) {
                        continue;
                    }
                    return new Command(m, argument);
                }
            }
        }
    }

    private Integer readArgument(int digits) throws IOException {
        StringBuilder buffer = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            char digit = readCharBlocking();
            if (!Character.isDigit(digit)) {
                return null;
            }
            buffer.append(digit);
        }
        return Integer.parseInt(buffer.toString());
    }

    private char readCharBlocking() throws IOException {
        int inputByte;
        do {
            inputByte = input.read();
        } while (inputByte < 0);
        if (echo) {
            output.write(inputByte);
        }
        return (char)inputByte;
    }
}
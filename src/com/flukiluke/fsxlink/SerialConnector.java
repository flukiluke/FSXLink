package com.flukiluke.fsxlink;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerialConnector {
    private SerialPort serialPort;
    private InputStream input;
    private OutputStream output;
    private List<Mapping> mappingList = new ArrayList<>();
    private boolean echo;

    public SerialConnector() throws IOException {
        Config serialConfig = Config.getConfig().getMap(Config.SERIAL);
        echo = serialConfig.getBoolean(Config.ECHO);
        try {
            serialPort = (SerialPort) CommPortIdentifier
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
        mappingList.add(m);
    }

    public Command readCommand() throws IOException {
        StringBuilder buffer = new StringBuilder(10);
        do {
            buffer.append(readCharBlocking());
            for (Mapping m : mappingList) {
                if (m.command.contentEquals(buffer)) {
                    if (m.digits == 0) {
                        return new Command(m);
                    }
                    else {
                        return new Command(m, readArgument(m.digits));
                    }
                }
            }
        } while (buffer.length() < buffer.capacity());
        throw new IOException("Received junk from serial device");
    }

    private int readArgument(int digits) throws IOException {
        StringBuilder buffer = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            char digit = readCharBlocking();
            if (!Character.isDigit(digit)) {
                throw new IOException("Expected digit from serial device");
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

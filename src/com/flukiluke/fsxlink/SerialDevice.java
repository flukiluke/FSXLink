package com.flukiluke.fsxlink;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialDevice extends Thread {
    public static final int SERIAL_TIMEOUT = 1000;
    private static final int READ_BUFFER_SIZE = 10;
    protected InputStream input;
    protected OutputStream output;
    private PrefixTree<Mapping> mappings = new PrefixTree<>();
    protected SerialManager serialManager;
    public String deviceName;
    public String portName;

    public SerialDevice() {
        // Exists only to satisfy Java's annoying rules for constructors & inheritance
    }

    public SerialDevice(SerialManager serialManager, CommPortIdentifier portId, int baud) throws IOException {
        this.serialManager = serialManager;
        portName = portId.getName();
        try {
            SerialPort serialPort = (SerialPort) portId.open(getClass().getName(), SERIAL_TIMEOUT);
            serialPort.setSerialPortParams(baud,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
        }
        catch (PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException("Connection to serial device at " + portName + " failed: " + e.toString());
        }
        sendHandshake();
        start();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                readCommand();
            }
            catch (IOException e) {
                System.err.println(getHumanName() + ": " + e.getLocalizedMessage());
                interrupt();
            }
        }
    }

    public void registerInputMapping(Mapping m) {
        mappings.add(m.code, m);
    }

    public void sendCommand(Command command) {
        System.out.println(getHumanName() + " <- " + command);
        try {
            output.write((command.toString() + '\n').getBytes());
        }
        catch (IOException e) {
            System.err.println(getHumanName() + ": Serial communications error");
            System.exit(1);
        }
    }

    public String getHumanName() {
        return deviceName == null ? portName : deviceName;
    }

    private void readCommand() throws IOException {
        String buffer = readLine();
        System.out.println(getHumanName() + " -> " + buffer);

        if (buffer.length() > 1 && buffer.charAt(0) == '@') {
            deviceName = buffer.substring(1);
            return;
        }
        int codeLength = 1;
        while (codeLength <= buffer.length() && mappings.isValidPrefix(buffer.substring(0, codeLength))) {
            codeLength++;
        }
        codeLength--;

        Mapping m = mappings.get(buffer.substring(0, codeLength));
        if (m == null) {
            System.err.println(getHumanName() + ": unknown command: " + buffer);
            return;
        }

        if (codeLength == buffer.length()) {
            serialManager.addReceivedCommand(new Command(m));
            return;
        }

        Integer argument;
        try {
            argument = Integer.parseInt(buffer.substring(codeLength));
        }
        catch (NumberFormatException e) {
            System.err.println(getHumanName() + ": argument is not an integer: " + buffer.substring(codeLength));
            return;
        }
        serialManager.addReceivedCommand(new Command(m, argument));
    }

    private void sendHandshake() throws IOException {
        output.write("?\n".getBytes());
    }

    private String readLine() throws IOException {
        StringBuilder buffer = new StringBuilder(READ_BUFFER_SIZE);

        // Read in data until newline
        while(true)  {
            char c = readCharBlocking();
            if (c == '\n') {
                break;
            }
            buffer.append(c);
            if (buffer.length() > READ_BUFFER_SIZE) {
                System.err.println(getHumanName()+ ": serial read buffer full without newline marker, emptying buffer");
                buffer.delete(0, buffer.length());
            }
        }
        return buffer.toString();
    }

    private Character readCharBlocking() throws IOException {
        int inputByte;
        do {
            inputByte = input.read();
        } while (inputByte < 0 || inputByte == '\r');
        return (char)inputByte;
    }
}
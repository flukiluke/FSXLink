package com.flukiluke.fsxlink;

import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialDevice extends Thread {
    public static final int SERIAL_TIMEOUT = 1000;
    private static final int READ_BUFFER_SIZE = 10;
    private InputStream input;
    private OutputStream output;
    private PrefixTree<Mapping> mappings = new PrefixTree<>();
    public String deviceName;

    public SerialDevice(CommPortIdentifier portid, int baud) throws IOException {
        try {
            SerialPort serialPort = (SerialPort) portid.open(getClass().getName(), SERIAL_TIMEOUT);
            serialPort.setSerialPortParams(baud,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
        }
        catch (PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException("Connection to serial device at " + portid.getName()
                    + " failed: " + e.toString());
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
                System.err.println(e);
                interrupt();
            }
        }
    }

    public SerialDevice() throws IOException {
        Config serialConfig = Config.getConfig().getMap(Config.SERIAL);
        String address = serialConfig.getString(Config.DEVICE);
        if (address.equals("console")) {
            input = System.in;
            output = System.out;
            return;
        }
        try {
            SerialPort serialPort = (SerialPort) CommPortIdentifier
                    .getPortIdentifier(address)
                    .open(getClass().getName(), 3);
            serialPort.setSerialPortParams(serialConfig.getInteger(Config.BAUD),
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
        }
        catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException("Connection to serial device at " + serialConfig.getString(Config.DEVICE)
                                    + " failed: " + e.toString());
        }
        sendHandshake();
        start();
    }

    public void registerInputMapping(Mapping m) {
        mappings.add(m.code, m);
    }

    public void sendCommand(Command command) {
        System.out.println(command);
        try {
            output.write((command.toString() + '\n').getBytes());
        }
        catch (IOException e) {
            System.err.println("Serial communications error");
            System.exit(1);
        }
    }

    public Command readCommand() throws IOException {
        String buffer = readLine();
        if (buffer.length() > 1 && buffer.charAt(0) == '@') {
            deviceName = buffer.substring(1);
            return null;
        }
        System.out.println("        " + buffer);
        int codeLength = 1;
        while (codeLength <= buffer.length() && mappings.isValidPrefix(buffer.substring(0, codeLength))) {
            codeLength++;
        }
        codeLength--;

        Mapping m = mappings.get(buffer.substring(0, codeLength));
        if (m == null) {
            System.err.println("Got unknown command from serial device: " + buffer);
            return null;
        }

        if (codeLength == buffer.length()) {
            return new Command(m);
        }

        Integer argument;
        try {
            argument = Integer.parseInt(buffer.substring(codeLength));
        }
        catch (NumberFormatException e) {
            System.err.println("Argument from serial device is not an integer: " + buffer.substring(codeLength));
            return null;
        }
        return new Command(m, argument);
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
                System.err.println("Serial read buffer full without newline marker, emptying buffer");
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
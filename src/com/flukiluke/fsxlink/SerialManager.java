package com.flukiluke.fsxlink;

import purejavacomm.CommPortIdentifier;

import java.io.IOException;
import java.util.*;

public class SerialManager implements CommandHandler {
    private List<SerialDevice> devices = new ArrayList<>();
    private final Queue<Command> receivedCommands = new ArrayDeque<>();

    public SerialManager(Config config) {
        if (config.getBoolean(Config.CONSOLE, false)) {
            devices.add(new ConsoleSerialDevice(this));
        }
        else {
            probePorts(config.getUnilistOfStrings(Config.PROBE));
        }
    }

    private void probePorts(List<String> usablePorts)  {
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        while (portIdentifiers.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portIdentifiers.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL &&
                    (usablePorts.size() == 0 || usablePorts.contains(portId.getName()))) {
                probe(portId);
            }
        }
        System.err.println("Identifying devices");
        try {
            Thread.sleep(SerialDevice.SERIAL_TIMEOUT*4);
        }
        catch (InterruptedException e) {
            System.exit(1);
        }

        removeUnknownDevices();
        if (devices.size() == 0) {
            System.err.println("No suitable devices identified");
            System.exit(1);
        }
    }

    @Override
    public void handleCommand(Command c) {
        for (SerialDevice d : devices) {
            if (c.mapping.receivers.size() == 0 || c.mapping.receivers.contains(d.deviceName) || d.deviceName == "CONSOLE") {
                d.sendCommand(c);
            }
        }
    }

    public void registerInputMapping(Mapping m) {
        devices.forEach(d -> d.registerInputMapping(m));
    }

    public Command readCommand() {
        synchronized (receivedCommands) {
            while (receivedCommands.size() == 0) {
                try {
                    receivedCommands.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return receivedCommands.poll();
        }
    }

    protected void addReceivedCommand(Command c) {
        synchronized (receivedCommands) {
            receivedCommands.add(c);
            receivedCommands.notifyAll();
        }
    }

    private void probe(CommPortIdentifier portId) {
        System.err.println("Probing " + portId.getName());
        try {
            devices.add(new SerialDevice(this, portId, Config.getConfig().getMap(Config.SERIAL).getInteger(Config.BAUD)));
        }
        catch (IOException e) {
            System.err.println(portId.getName() + " failed: " + e.getLocalizedMessage());
        }
        System.err.println(portId.getName() + " connected");
    }

    private void removeUnknownDevices() {
        for (SerialDevice d : devices) {
            if (d.deviceName == null) {
                d.interrupt();
            }
            else {
                System.err.println("Detected device " + d.deviceName + " on " + d.portName);
            }
        }
        devices.removeIf(Thread::isInterrupted);
    }
}

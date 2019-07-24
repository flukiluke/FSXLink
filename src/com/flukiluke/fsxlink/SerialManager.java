package com.flukiluke.fsxlink;

import purejavacomm.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SerialManager {
    public void probePorts()  {
        List<SerialDevice> devices = new ArrayList<>();
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        while (portIdentifiers.hasMoreElements()) {
            CommPortIdentifier portid = (CommPortIdentifier) portIdentifiers.nextElement();
            if (portid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                System.err.print("Probing " + portid.getName() + "...");
                try {
                    devices.add(new SerialDevice(portid, Config.getConfig().getMap(Config.SERIAL).getInteger(Config.BAUD)));
                }
                catch (IOException e) {
                    System.err.println("failed: " + e.getLocalizedMessage());
                }
                System.err.println("connected");
            }
        }
        System.err.print("Identifying devices...");
        try {
            Thread.sleep(SerialDevice.SERIAL_TIMEOUT*3);
        }
        catch (InterruptedException e) {
            System.exit(1);
        }
        System.err.println("done");
        for (SerialDevice d : devices) {
            if (d.deviceName == null) {
                d.interrupt();
            }
            else {
                System.err.println("Detected device " + d.deviceName);
            }
        }
        devices.removeIf(Thread::isInterrupted);
        if (devices.size() == 0) {
            System.err.println("No suitable devices identified");
            System.exit(1);
        }
    }

}

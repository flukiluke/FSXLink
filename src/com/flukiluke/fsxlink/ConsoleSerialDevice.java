package com.flukiluke.fsxlink;

public class ConsoleSerialDevice extends SerialDevice {
    public ConsoleSerialDevice(SerialManager serialManager) {
        deviceName = "CONSOLE";
        this.serialManager = serialManager;
        this.input = System.in;
        this.output = System.out;
        start();
    }
}

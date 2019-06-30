package com.flukiluke.fsxlink;

public class Mapping {
    private String simconnectName;
    private String serialCommand;
    private int argLength;

    public Mapping(String simconnectName, String serialCommand, int argLength) {
        this.simconnectName = simconnectName;
        this.serialCommand = serialCommand;
        this.argLength = argLength;
    }
}

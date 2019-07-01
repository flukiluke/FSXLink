package com.flukiluke.fsxlink;

public class Mapping {
    public final String simconnectName;
    public final String unit;
    public final String serialCommand;
    public final long argLength;
    public Integer scID = null; // Assigned once this mapping is made known to FSX

    public Mapping(String simconnectName, String unit, String serialCommand, long argLength) {
        this.simconnectName = simconnectName;
        this.unit = unit;
        this.serialCommand = serialCommand;
        this.argLength = argLength;
    }
}

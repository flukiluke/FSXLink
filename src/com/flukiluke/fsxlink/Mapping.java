package com.flukiluke.fsxlink;

public class Mapping {
    public final String simconnectName;
    public final String serialCommand;
    public final long argLength;
    public Integer eventId = null; // Assigned once this mapping is made known to FSX

    public Mapping(String simconnectName, String serialCommand, long argLength) {
        this.simconnectName = simconnectName;
        this.serialCommand = serialCommand;
        this.argLength = argLength;
    }
}

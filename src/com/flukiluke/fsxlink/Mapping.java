package com.flukiluke.fsxlink;

public class Mapping {
    public final String inputName;
    public final String outputName;
    public final String command;
    public final String unit;
    public final Integer digits;
    public final boolean isToggle;

    public Integer eventId = 0;

    public Mapping(Config c) {
        this.inputName = c.getString(Config.INPUT);
        this.outputName = c.getString(Config.OUTPUT);
        this.command = c.getString(Config.COMMAND);
        if (this.command == null) {
            throw new IllegalArgumentException("No command specified for mapping");
        }
        if (c.getString(Config.UNIT).equals(Config.TOGGLE)) {
            this.unit = "boolean";
            this.isToggle = true;
        }
        else {
            this.unit = c.getString(Config.UNIT);
            this.isToggle = false;
        }
        this.digits = c.getInteger(Config.DIGITS, 0);
    }

    public boolean isInput() {
        return inputName != null;
    }

    public boolean isOutput() {
        return outputName != null;
}
}

package com.flukiluke.fsxlink;

public class Mapping {
    public final String inputName;
    public final String outputName;
    public final String code;
    public final String unit;
    public final Integer digits;
    public final boolean isToggle;

    public Integer eventId = 0;

    public Mapping(Config c) {
        this.inputName = c.getString(Config.INPUT);
        this.outputName = c.getString(Config.OUTPUT);
        this.code = c.getString(Config.CODE);
        if (this.code == null) {
            throw new IllegalArgumentException("No code specified for mapping");
        }
        if (c.getString(Config.UNIT) == null) {
            this.unit = "";
            this.isToggle = false;
        }
        else if (c.getString(Config.UNIT).equals(Config.TOGGLE)) {
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

package com.flukiluke.fsxlink;

import java.util.List;

public class Mapping {
    public final List<String> inputNames;
    public final String outputName;
    public final String code;
    public final String unit;
    public final boolean isFloat;
    public final boolean isToggle;

    public Integer baseEventId = 0;

    public Mapping(List<String> inputNames, String outputName, String code, String unit, String type) {
        this.inputNames = inputNames;
        this.outputName = outputName;
        this.code = code;
        this.unit = unit;
        this.isFloat = type.equalsIgnoreCase("float");
        this.isToggle = false;
    }

    public Mapping(Config c) {
        this.inputNames = c.getUnilistOfStrings(Config.INPUT);
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
        this.isFloat = c.getString(Config.TYPE, "int").equalsIgnoreCase("float");
    }

    public boolean isInput() {
        return inputNames != null;
    }

    public boolean isOutput() {
        return outputName != null;
    }
}

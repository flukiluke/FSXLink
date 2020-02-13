package com.flukiluke.fsxlink;

public class Command {
    public final Mapping mapping;
    public final boolean hasArgument;
    public final Number argument;

    public Command(Mapping mapping) {
        this.mapping = mapping;
        this.hasArgument = false;
        this.argument = 0;
    }

    public Command(Mapping mapping, Integer argument) {
        this.mapping = mapping;
        this.hasArgument = true;
        this.argument = argument;
    }

    public Command(Mapping mapping, Double argument) {
        this.mapping = mapping;
        this.hasArgument = true;
        this.argument = argument;
    }

    @Override
    public String toString() {
        if (!hasArgument && !mapping.isToggle) {
            return mapping.code;
        }
        if (mapping.isFloat && mapping.round != null) {
            return mapping.code + String.format("%." + mapping.round + "f", argument);
        }
        else {
            return mapping.code + argument.toString();
        }
    }
}

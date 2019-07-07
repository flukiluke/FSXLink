package com.flukiluke.fsxlink;

public class Command {
    public final Mapping mapping;
    public final boolean hasArgument;
    public final int argument;

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

    @Override
    public String toString() {
        if (!hasArgument && !mapping.isToggle) {
            return mapping.command;
        }
        int digits = mapping.isToggle ? 1 : mapping.digits;
        return mapping.command + String.format("%0" + digits + "d", argument);
    }
}

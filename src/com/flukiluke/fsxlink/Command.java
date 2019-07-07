package com.flukiluke.fsxlink;

public class Command {
    public final Mapping mapping;
    public final Integer argument;

    public Command(Mapping mapping) {
        this.mapping = mapping;
        this.argument = null;
    }

    public Command(Mapping mapping, Integer argument) {
        this.mapping = mapping;
        this.argument = argument;
    }

    @Override
    public String toString() {
        if (argument == null) {
            return mapping.command;
        }
        return mapping.command + String.format("%0" + mapping.digits + "d", argument);
    }
}

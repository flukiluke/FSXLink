package com.flukiluke.fsxlink;

public class Command {
    public final Mapping mapping;
    public final Integer argument;

    public Command(Mapping mapping) {
        this.mapping = mapping;
        this.argument = 0;
    }

    public Command(Mapping mapping, Integer argument) {
        this.mapping = mapping;
        this.argument = argument;
    }
}

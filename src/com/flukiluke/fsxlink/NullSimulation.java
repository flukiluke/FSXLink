package com.flukiluke.fsxlink;

import java.io.IOException;

public class NullSimulation implements Simulation {
    @Override
    public void startDataHandler(SerialDevice sink) {

    }

    @Override
    public void registerInputMapping(Mapping mapping) throws IOException {

    }

    @Override
    public void registerOutputMapping(Mapping mapping) throws IOException {

    }

    @Override
    public void sendEvent(Command command) throws IOException {

    }
}

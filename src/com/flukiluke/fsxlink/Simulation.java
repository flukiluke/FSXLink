package com.flukiluke.fsxlink;

import java.io.IOException;

public interface Simulation {
    void startDataHandler(CommandHandler handler);

    void registerInputMapping(Mapping mapping) throws IOException;

    void registerOutputMapping(Mapping mapping) throws IOException;

    void sendEvent(Command command) throws IOException;
}

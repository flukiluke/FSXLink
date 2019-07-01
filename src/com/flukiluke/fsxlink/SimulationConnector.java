package com.flukiluke.fsxlink;

import flightsim.simconnect.NotificationPriority;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;

import java.io.IOException;

public class SimulationConnector {
    private SimConnect simConnect;
    private int nextEventId = 1; // Examples start this value at 1

    public SimulationConnector() throws IOException {
        Configuration simConnectConfig = Config.getSimConnectConfig();
        simConnect = new SimConnect(simConnectConfig.get("appName"),
                simConnectConfig,
                simConnectConfig.getInt("simConnectProtocol", 2));
        DispatcherTask dt = new DispatcherTask(simConnect);
    }

    public int registerInputMapping(Mapping mapping) throws IOException {
        simConnect.mapClientEventToSimEvent(nextEventId, mapping.simconnectName);
        mapping.eventId = nextEventId;
        return nextEventId++;
    }

    public void sendEvent(Mapping mapping, int data) throws IOException {
        simConnect.transmitClientEvent(SimConnectConstants.OBJECT_ID_USER,
                mapping.eventId,
                data,
                NotificationPriority.DEFAULT.ordinal(),
                SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
    }
}

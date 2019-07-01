package com.flukiluke.fsxlink;

import flightsim.simconnect.*;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;

import java.io.IOException;

public class SimulationConnector {
    private SimConnect simConnect;
    private int nextEventId = 1; // Examples start these values at 1
    private int nextDataId = 1;

    public SimulationConnector() throws IOException {
        Configuration simConnectConfig = Config.getSimConnectConfig();
        simConnect = new SimConnect(simConnectConfig.get("appName"),
                simConnectConfig,
                simConnectConfig.getInt("simConnectProtocol", 2));
        DispatcherTask dt = new DispatcherTask(simConnect);
        dt.addSimObjectDataHandler((sender, e) -> System.out.println("Value = " + e.getDataFloat64()));
        new Thread(dt).start();
    }

    public void registerInputMapping(Mapping mapping) throws IOException {
        simConnect.mapClientEventToSimEvent(nextEventId, mapping.simconnectName);
        mapping.scID = nextEventId++;
    }

    public void registerOutputMapping(Mapping mapping) throws IOException {
        simConnect.addToDataDefinition(nextDataId,
                mapping.simconnectName,
                mapping.unit,
                SimConnectDataType.FLOAT64);
        simConnect.requestDataOnSimObject(nextDataId,
                nextDataId,
                0,
                SimConnectPeriod.SIM_FRAME,
                SimConnectConstants.DATA_REQUEST_FLAG_CHANGED,
                0, 0,0);
        nextDataId++;
    }

    public void sendEvent(Mapping mapping, int data) throws IOException {
        simConnect.transmitClientEvent(SimConnectConstants.OBJECT_ID_USER,
                mapping.scID,
                data,
                NotificationPriority.DEFAULT.ordinal(),
                SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
    }
}

package com.flukiluke.fsxlink;

import flightsim.simconnect.*;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimulationConnector {
    private SimConnect simConnect;
    private int nextEventID = 1;
    private List<Mapping> dataMappings = new ArrayList<>();

    public SimulationConnector() throws IOException {
        // Fill the 0 slot so that we can start counting from 1
        dataMappings.add(null);
        Configuration simConnectConfig = Config.getSimConnectConfig();
        simConnect = new SimConnect(simConnectConfig.get("appName"),
                simConnectConfig,
                simConnectConfig.getInt("simConnectProtocol", 2));
        DispatcherTask dt = new DispatcherTask(simConnect);
        dt.addSimObjectDataHandler((sender, e) -> {
            Mapping mapping = dataMappings.get(e.getDefineID());
            String value = String.format("%0" + mapping.argLength + "d", e.getDataInt32());
            System.out.println(mapping.serialCommand + value);
        });
        new Thread(dt).start();
    }

    public void registerInputMapping(Mapping mapping) throws IOException {
        mapping.scID = nextEventID++;
        simConnect.mapClientEventToSimEvent(mapping.scID, mapping.simconnectName);
    }

    public void registerOutputMapping(Mapping mapping) throws IOException {
        dataMappings.add(mapping);
        mapping.scID = dataMappings.size() - 1;
        simConnect.addToDataDefinition(mapping.scID,
                mapping.simconnectName,
                mapping.unit,
                SimConnectDataType.INT32);
        simConnect.requestDataOnSimObject(mapping.scID,
                mapping.scID,
                0,
                SimConnectPeriod.SIM_FRAME,
                SimConnectConstants.DATA_REQUEST_FLAG_CHANGED,
                0, 0,0);
    }

    public void sendEvent(Mapping mapping, int data) throws IOException {
        simConnect.transmitClientEvent(SimConnectConstants.OBJECT_ID_USER,
                mapping.scID,
                data,
                NotificationPriority.DEFAULT.ordinal(),
                SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
    }
}

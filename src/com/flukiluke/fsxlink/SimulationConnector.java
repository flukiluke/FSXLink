package com.flukiluke.fsxlink;

import flightsim.simconnect.*;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;

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
        Config loadedConfig = Config.getConfig().getMap(Config.SIMCONNECT);
        Configuration simConnectConfig = new Configuration();
        simConnectConfig.setAddress(loadedConfig.getString(Config.IP));
        simConnectConfig.setProtocol(loadedConfig.getInteger(Config.IPVERSION));
        simConnectConfig.setPort(loadedConfig.getInteger(Config.PORT));

        simConnect = new SimConnect(loadedConfig.getString(Config.APPNAME),
                simConnectConfig,
                loadedConfig.getInteger(Config.PROTOCOL));
    }

    public void startDataHandler(DataCommandSink sink) {
        DispatcherTask dt = new DispatcherTask(simConnect);
        dt.addSimObjectDataHandler(new DataHandler(sink));
        Thread t = new Thread(dt);
        t.setDaemon(true);
        t.start();
    }

    public void registerInputMapping(Mapping mapping) throws IOException {
        mapping.eventId = nextEventID++;
        simConnect.mapClientEventToSimEvent(mapping.eventId, mapping.inputName);
    }

    public void registerOutputMapping(Mapping mapping) throws IOException {
        dataMappings.add(mapping);
        int dataId = dataMappings.size() - 1;
        simConnect.addToDataDefinition(dataId,
                mapping.outputName,
                mapping.unit,
                SimConnectDataType.INT32);
        simConnect.requestDataOnSimObject(dataId,
                dataId,
                0,
                SimConnectPeriod.SIM_FRAME,
                SimConnectConstants.DATA_REQUEST_FLAG_CHANGED,
                0, 0,0);
    }

    public void sendEvent(Command command) throws IOException {
        simConnect.transmitClientEvent(SimConnectConstants.OBJECT_ID_USER,
                command.mapping.eventId,
                command.argument,
                NotificationPriority.DEFAULT.ordinal(),
                SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
    }

    private class DataHandler implements SimObjectDataHandler {
        private DataCommandSink sink;

        public DataHandler(DataCommandSink sink) {
            this.sink = sink;
        }

        @Override
        public void handleSimObject(SimConnect sender, RecvSimObjectData e) {
            Mapping m = dataMappings.get(e.getDefineID());
            if (m.unit == null) {
                sink.sendCommand(new Command(m));
            } else {
                sink.sendCommand(new Command(m, e.getDataInt32()));
            }
        }
    }
}

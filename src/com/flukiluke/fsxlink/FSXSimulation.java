package com.flukiluke.fsxlink;

import flightsim.simconnect.*;
import flightsim.simconnect.config.Configuration;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FSXSimulation implements Simulation {
    private SimConnect simConnect;
    private int nextEventID = 1;
    private List<Mapping> dataMappings = new ArrayList<>();

    private Configuration makeConnectionConfig(Config loadedConfig) {
        Configuration simConnectConfig = new Configuration();
        if (loadedConfig.getString(Config.IP) == null) {
            simConnectConfig.setAddress("::1");
            simConnectConfig.setPort(Configuration.findSimConnectPortIPv6());
            simConnectConfig.setProtocol(6);
        }
        else {
            simConnectConfig.setAddress(loadedConfig.getString(Config.IP));
            simConnectConfig.setPort(loadedConfig.getInteger(Config.PORT));
            simConnectConfig.setProtocol(loadedConfig.getString(Config.IP).indexOf(':') >= 0 ? 6 : 4);
        }
        return simConnectConfig;
    }

    public FSXSimulation() throws IOException {
        // Fill the 0 slot so that we can start counting from 1
        dataMappings.add(null);
        Config loadedConfig = Config.getConfig().getMap(Config.SIMCONNECT);
        Configuration simConnectConfig = makeConnectionConfig(loadedConfig);

        simConnect = new SimConnect(loadedConfig.getString(Config.APPNAME),
                simConnectConfig,
                2); // Version 2 is supported by all versions of FSX
    }

    @Override
    public void startDataHandler(CommandHandler handler) {
        DispatcherTask dt = new DispatcherTask(simConnect);
        dt.addSimObjectDataHandler(new DataHandler(handler));
        Thread dispatcherThread = new Thread(dt);
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
    }

    @Override
    public void registerInputMapping(Mapping mapping) throws IOException {
        mapping.baseEventId = nextEventID;
        for (String m : mapping.inputNames) {
            simConnect.mapClientEventToSimEvent(nextEventID++, m);
        }
    }

    @Override
    public void registerOutputMapping(Mapping mapping) throws IOException {
        dataMappings.add(mapping);
        int dataId = dataMappings.size() - 1;
        simConnect.addToDataDefinition(dataId,
                mapping.outputName,
                mapping.unit,
                mapping.isFloat ? SimConnectDataType.FLOAT64 : SimConnectDataType.INT32);
        simConnect.requestDataOnSimObject(dataId,
                dataId,
                0,
                SimConnectPeriod.SIM_FRAME,
                SimConnectConstants.DATA_REQUEST_FLAG_CHANGED,
                0, 0,0);
    }

    @Override
    public void sendEvent(Command command) throws IOException {
        for (int i = 0; i < command.mapping.inputNames.size(); i++) {
            simConnect.transmitClientEvent(SimConnectConstants.OBJECT_ID_USER,
                    command.mapping.baseEventId + i,
                    (Integer)command.argument,
                    NotificationPriority.DEFAULT.ordinal(),
                    SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY);
        }
    }

    private class DataHandler implements SimObjectDataHandler {
        private CommandHandler sink;

        public DataHandler(CommandHandler sink) {
            this.sink = sink;
        }

        @Override
        public void handleSimObject(SimConnect sender, RecvSimObjectData e) {
            Mapping m = dataMappings.get(e.getDefineID());
            if (m.unit == null) {
                sink.handleCommand(new Command(m));
            } else {
                Command c;
                if (m.isFloat) {
                    c = new Command(m, e.getDataFloat64());
                }
                else {
                    c = new Command(m, e.getDataInt32());
                }
                sink.handleCommand(c);
            }
        }
    }
}

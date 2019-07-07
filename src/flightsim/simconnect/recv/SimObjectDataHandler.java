package flightsim.simconnect.recv;

import flightsim.simconnect.SimConnect;

public interface SimObjectDataHandler {
	void handleSimObject(SimConnect sender, RecvSimObjectData e);

}

# FSXLink #

## About ##
FSXLink is a tool capable of talking to both Microsoft Flight Simulator X (FSX) and serial-oriented devices such as Arduino boards, the idea being to control FSX and display simulation data with the Arduino. This is still a work in progress.

## Config file ##
A YAML file - edit the config.yml as needed.

- `simConnect.appName` Arbitrary text field to identify the program connecting to FSX. Not actually shown anywhere, as far as I know.

If you are running FSXLink on the same computer as FSX, you don't need these next two options. They are only needed if you are connecting to FSX over a network.
- `simConnect.ip` IP Address of remote computer running FSX. Supports IPv4 and IPv6 addresses.
- `simConnect.port` The port FSX is listening on. By default FSX picks a port at random; you can change that behaviour with [SimConnect Config Tool](https://github.com/jtreml/simconnect-config-tool).

The `serial` block sets configuration for the serial device. Currently we only support a single device.
- `serial.device` Device file to open for serial communications.
- `serial.baud` Baud rate to communicate at. Lower values may help if your device cannot process output fast enough.
- `serial.console` If set to true, treat the programs' standard input & output as a serial device, so you can send commands interactively. This pseudo-device always identifies itself as `CONSOLE` and receives all outputs regardless of the `for` filter.

`mappings` specifies how FSX events and data attributes are conveyed to the serial device. Some attributes of a mapping are optional.
- `code` One or more characters that are used in the serial communication protocol, in both directions. Any code must not be the prefix of another code, otherwise everyone will get very confused and sad.
- `input` FSX's name for the event, found on [Event IDs](https://www.prepar3d.com/SDKv4/sdk/references/variables/event_ids.html) in the SimConnect column. When the serial device transmits appropriately, this event will be generated in FSX. If omitted, the serial device cannot send this mappings's code.
- `output` FSX's name for the variable, taken from [Simulation Variables](https://www.prepar3d.com/SDKv4/sdk/references/variables/simulation_variables.html). The value of this variable will be transmitted to the serial device when it changes. If omitted, the serial device will never receive this mapping's code.
- `unit` Numeric data will be in a particular unit, such as 'feet' or 'knots'. This attribute what unit the value should be expressed in. Only needs to be set if `output` is set. Also see section on Toggles below.
- `for` If specified, is a string or list of strings that are the names of connected serial devices, as discovered by the device interrogation protocol described below. The output will only be sent to devices that whose name is one of those provided. If this option is not specified, the output is sent to all devices. The special CONSOLE device always receives all outputs regardless of this setting. There is no effect on inputs.

If the value is a floating-point number, the following options also apply:
- `type` should be set to `float`
- `round` is the number of decimal places to round the number to. If omitted, no rounding is done and you get very long numbers.

## Serial Protocol ##
Commands sent to the serial device consist of the `code` string, followed by a numeric argument (if present), then finally a newline (ASCII 10, '\n') character. In a similar fashion, commands from the serial device should consist of the `code` string, any numeric argument, then the newline character.

On start, FSXLink will interrogate the serial device by ending the character '?' and a newline. The device should reply with the '@' character, followed by a string identifier and a newline. The identifier may be up to 9 characters long. Currently the identifier is not used for anything, but in future will be used for multiple device support. The device may also send the '@' + identifier command unsolicited at any time.

### Toggles ###
Some functions of FSX are simple on/off switches. If the event used for controlling it is described in the documentation as a "toggle" event and you are also outputting that variable, set the `unit` attribute to `toggle`.

When the serial device wishes to toggle the value, it should send the `code` string with no numeric argument. When FSXLinks reports back a change, it will send will a one-digit argument, either a 1 or 0 for on or off, respectively.

If instead the input event is described as setting a switch to a particular position, set the `unit` to `boolean` and send a '1' or '0' argument as needed.

## Licence ##
FSXLink is released under the terms of the GNU General Public License version 3, contained in the COPYING file.

This program includes [jsimconnect](https://github.com/mharj/jsimconnect), located in src/flightsim/ and doc/jsimconnect/. It is used and redistributed under the terms of the GNU Lesser General Public License version 2.1, contained in the COPYING.LESSER file.
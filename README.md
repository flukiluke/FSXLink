# FSXLink #

## About ##
FSXLink is a tool capable of talking to both Microsoft Flight Simulator X (FSX) and serial-oriented devices such as Arduino boards, the idea being to control FSX and display simulation data with the Arduino. This is still a work in progress, and currently only gives you a command-line interface to talk to FSX.

## Config file ##
Simple json - copy the example config.json.
- `simconnect.app_name` Arbitrary text field to identify the program connecting to FSX. Not actually shown anywhere, as far as I know.
- `simconnect.ip` IP Address of FSX. This program can connect to instances running on another computer, otherwise use the loopback address 127.0.0.1 (or ::1 for IPv6).
- `simconnect.ip_version` 4 for IPv4 or 6 for IPv6.
- `simconnect.port` The port FSX is listening on. If in doubt [TCPView](https://docs.microsoft.com/en-us/sysinternals/downloads/tcpview) can be used to find this value.
- `simconnect.protocol` There are multiple versions of the connection protocol, released with newer versions of FSX. See the jsimconnect for details; leave as "2" if in doubt.

The `input_map` specifies a list of events to be triggered in FSX when a command is received from the serial input.
- `name` FSX's name for the event, found on [Event IDs](https://www.prepar3d.com/SDKv4/sdk/references/variables/event_ids.html) in the SimConnect column.
- `command` The command to expect from the serial input. Commands may be one or more characters long, but take care to ensure all commands are prefix codes; that is, no command is a prefix of another.
- `arg_length` Some events take an integer value as an argument. This is the number of bytes after the command to interpret as a number string. If the command takes no argument, this is 0.

The `output_map` lists variables to report from FSX to the serial device.
- `name` FSX's name for the variable, taken from [Simulation Variables](https://www.prepar3d.com/SDKv4/sdk/references/variables/simulation_variables.html).
- `unit` The unit of measurement for the value.
- `command` The command to send to the serial device before the value. This may be one or more characters.
- `arg_length` The number of characters to send the numeric value as, padding with zeros if needed.

## Licence ##
FSXLink is released under the terms of the GNU General Public License version 3, contained in the COPYING file.

This program uses [jsimconnect](https://github.com/mharj/jsimconnect), located in src/flightsim/ and doc/jsimconnect/. It is used under the terms of the GNU Lesser General Public License version 2.1, contained in the COPYING.LESSER file.

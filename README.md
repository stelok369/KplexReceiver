# KplexReciver
App to recive NMEA0183 data from kplex and provide websocket interface

Copyright (C) 2016 Smoliy Artem<br>Contact: strelok369@yandex.ru

KplexReciver is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

KplexReciver is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with KplexReciver. If not, see <http://www.gnu.org/licenses/>.

### Features implemented 
- Reading kplex stdout
- Web interface for message log
- Retrieving last n messages
- Storing messages in 10MB circular buffer
- data websocket
- status websocket

### System structure
![Structure](/docs/struct_en.png?raw=true "Structure")

### Usage
1. Build with your favorit Java build system. You should use Java 8 system lib and ReactiveServer lib from https://github.com/stelok369/ReactiveServer
2. Install kplex from https://github.com/stripydog/kplex, config it for output NMEA0183 to stdout. Kplex should be in PATH
3. Wisit App's machine IP from browser. Firefox ang Chrome somehow working, other not tested.
4. Study web interface page scripts to write your telemetry app.

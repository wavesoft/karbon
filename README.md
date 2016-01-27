# Karbon Trace file Analyser

Karbon is a Graphical Profiler based on trace files produced by `strace` or `systemtap`. It has a modular design, allowing individual analysis plug-ins to be registered and used at any time.

Karbon is perfect for identifying performance issues related to **System Calls ONLY**. It is completely blind regarding the application code. It *can not* resolve symbols, *nor* detect software-induced performance penalties.

This means that after identifying the 'hot' system calls you will still need to go through your code and identify were the problem is located.

## Creating a trace file

Karbon works with most of trace file formats, however it's most optimal to create them with the following command line:

```
strace -TtttfFvx -s 256 -o trace.dmp -- /path/to/application args ...
```

If you prefer, you can live-trace the application through the Karbon GUI.

## Running Karbon

You will need __Java Runtime__ installed to your system. In most of the cases you will only need to double-click the `.jar`, but if you have trouble, try running it from the command-line:

```
java -jar Karbon.jar
```

### Current Plugins

The following list enumerates all the currently available plug-ins and their use:

- **Classifier** : Traces all system calls that perform operations on file descriptors, such as Filesystem (`fopen`,`fclose` etc.), Memory (`mmap`, `mprotect` etc.), Network (`socket`,`sockopt` ...) or Misc (`pipe`, `socketpair` etc.).
- **System Calls** : An overview plug-in that summarises the statistics from all system calls, such as error rate, time spent on errors and overall time spent. 
- **Socket Tracker** : A specialised plug-in that tracks socket creation and actions. This plugin also resolves the endpoint details (IP address or UNIX socket) and the traffic through them.
- **File Tracker** : A specialised plug-in that tracks individual files accessed by the application, including detailed I/O statistics.
- **Hotspot Tracker** : A specialised plug-in that identifies  operations that take too much time to complete, including System Calls, Filesystem Operations and Processes.
- **Process Tracker** : A specialised plug-in that tracks process creation and behaviour. It constructs a comprehensive process tree and evolution graph in order to identify fork-related problems.
- **Time Utilisation** : A specialised plug-in that summarises the time spent in various operations, such as File I/O or Network I/O.

## License

Karbon - Tracefile Analyzer GUI 
Copyright (C) 2010-2015 Ioannis Charalampidis

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


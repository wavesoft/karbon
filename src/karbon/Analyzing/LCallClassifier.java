/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.TPreprocessingTool;
import KAnalyzer.API.TPresenter;
import KAnalyzer.ErrorCollector;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Utils.FDTracker;
import KAnalyzer.Utils.FDTracker.DynamicDescriptor;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * Make sure you are sobber enough before you start reading
 * this class!!!
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class LCallClassifier extends TPreprocessingTool {

    // Instance the file descriptor trackers
    private Hashtable<Long, FDTracker> socketFD = new Hashtable<Long, FDTracker>();
    private Hashtable<Long, FDTracker> fileFD = new Hashtable<Long, FDTracker>();
    private Hashtable<Long, FDTracker> memoryFD = new Hashtable<Long, FDTracker>();
    private Hashtable<Long, FDTracker> miscFD = new Hashtable<Long, FDTracker>();

    // The unknown calls
    public ArrayList<String> unknownCalls = new ArrayList<String>();

    // The summary object and instance
    private class IOSummary {
        public Long totalByesIn = 0l;
        public Long totalByesOut = 0l;
        public Integer totalHits = 0;
        public Double  totalTime = 0.0;
        public String  worstInstruction = "";
        public Double  worstTime = 0.0;
        public Integer totalFDs = 0;
        public Integer totalFailures = 0;
        public Double  totalFailedTime = 0.0;
    }

    // The error definition class
    private class IOErrors {
        public Long code = 0l;
        public String shorName = "";
        public String longName = "";
        public Integer hits = 0;
        public Double totalTime = 0.0;
    }

    // The most favoured errors :)
    private Hashtable<String, IOErrors> errorList = new Hashtable<String, IOErrors>();

    /*
     * PID Mapping for the <clone> system call
     *
     * This is a special case, because clone() also clones all
     * the open file descriptors.
     */
    public Hashtable<Long, Long> cloneMapping = new Hashtable<Long, Long>();

    /*
     * Accumulated call time
     */
    public Double totalTime = 0d;

    // My presenter
    private PResources presenter;

    public LCallClassifier() {
        presenter = new PResources();
    }

    /*
     * Fetch or create the FDTracker for the specified PID
     */
    private FDTracker getSocketFDTracker(Long forPID) {
        // Cloned apps have shared resources, so remap a child PID to the root PID
        Long pid = forPID;
        if (cloneMapping.containsKey(pid)) pid = cloneMapping.get(pid);
        if (socketFD.containsKey(pid)) {
            return socketFD.get(pid);
        } else {
            FDTracker tracker = new FDTracker();
            socketFD.put(pid, tracker);
            return tracker;
        }
    }
    private FDTracker getMiscFDTracker(Long forPID) {
        // Cloned apps have shared resources, so remap a child PID to the root PID
        Long pid = forPID;
        if (cloneMapping.containsKey(pid)) pid = cloneMapping.get(pid);
        if (miscFD.containsKey(pid)) {
            return miscFD.get(pid);
        } else {
            FDTracker tracker = new FDTracker();
            miscFD.put(pid, tracker);
            return tracker;
        }
    }
    private FDTracker getFileFDTracker(Long forPID) {
        // Cloned apps have shared resources, so remap a child PID to the root PID
        Long pid = forPID;
        if (cloneMapping.containsKey(pid)) pid = cloneMapping.get(pid);
        if (fileFD.containsKey(pid)) {
            return fileFD.get(pid);
        } else {
            FDTracker tracker = new FDTracker();

            // Streate Standard I/O Streams
            tracker.mapFD("<stdin>", 0l).set("file", "<stdin>");
            tracker.mapFD("<stdout>", 1l).set("file", "<stdout>");
            tracker.mapFD("<stderr>", 2l).set("file", "<stderr>");

            fileFD.put(pid, tracker);
            return tracker;
        }
    }
    private FDTracker getMemoryFDTracker(Long forPID) {
        // Cloned apps have shared resources, so remap a child PID to the root PID
        Long pid = forPID;
        if (cloneMapping.containsKey(pid)) pid = cloneMapping.get(pid);
        if (memoryFD.containsKey(pid)) {
            return memoryFD.get(pid);
        } else {
            FDTracker tracker = new FDTracker();
            memoryFD.put(pid, tracker);
            return tracker;
        }
    }

    /*
     * This is the flag/group information for the call table that
     * follows right after.
     *
     * I am using a call table and integer flags in order to speed-up
     * the procedure as much as possible, because this script is gonna
     * be called for every system call!
     * 
     */
    private static final Integer FLAG_USES_FD       = 0x000100;
    private static final Integer FLAG_RETURNS_FD    = 0x000200;
    private static final Integer FLAG_RELEASES_FD   = 0x000400;
    private static final Integer FLAG_HAS_STRING    = 0x000800;
    private static final Integer FLAG_SECOND_FD     = 0x001000;
    private static final Integer FLAG_SECOND_STRING = 0x002000;
    private static final Integer FLAG_CREATES_FD1   = 0x004000;
    private static final Integer FLAG_CREATES_FD2   = 0x008000;

    private static final Integer GROUP_SOCKET       = 0x010000;
    private static final Integer GROUP_FILE         = 0x020000;
    private static final Integer GROUP_MEMORY       = 0x040000;
    private static final Integer GROUP_MISC         = 0x080000;
    private static final Integer GROUP_SPECIAL      = 0x100000;
    private static final Integer GROUP_ALL          = 0x0F0000;

    private static final Integer MASK_GROUP         = 0xFF0000;
    private static final Integer MASK_FLAG          = 0x00FF00;
    private static final Integer MASK_ID            = 0x0000FF;

    private static final Object[][] syscallTable = {

        /** Really special system call ;) **/
        {"clone",          1   | GROUP_SPECIAL },

        /** Common calls **/
        {"close",         201  | GROUP_ALL | FLAG_USES_FD | FLAG_RELEASES_FD },
        {"dup",           202  | GROUP_ALL | FLAG_USES_FD | FLAG_RETURNS_FD },
        {"dup2",          203  | GROUP_ALL | FLAG_USES_FD | FLAG_RETURNS_FD | FLAG_CREATES_FD2 },
        {"write",         204  | GROUP_ALL | FLAG_USES_FD },
        {"pwrite",        205  | GROUP_ALL | FLAG_USES_FD },
        {"writev",        206  | GROUP_ALL | FLAG_USES_FD },
        {"read",          207  | GROUP_ALL | FLAG_USES_FD },
        {"pread",         208  | GROUP_ALL | FLAG_USES_FD },
        {"readv",         209  | GROUP_ALL | FLAG_USES_FD },
        {"lseek",         210  | GROUP_ALL | FLAG_USES_FD },
        {"poll",          220  | GROUP_ALL | FLAG_USES_FD },
        {"ppoll",         221  | GROUP_ALL | FLAG_USES_FD },
        {"ioctl",         222  | GROUP_ALL | FLAG_USES_FD },
        {"fcntl",         223  | GROUP_ALL | FLAG_USES_FD | FLAG_HAS_STRING | FLAG_RETURNS_FD },

        /** Misc calls **/
        {"pipe",            1  | GROUP_MISC | FLAG_CREATES_FD1 | FLAG_CREATES_FD2 },
        {"pipe2",           1  | GROUP_MISC | FLAG_CREATES_FD1 | FLAG_CREATES_FD2 },
        {"select",          2  | GROUP_MISC },
        {"socketpair",      3  | GROUP_MISC },
        {"epoll_create",    4  | GROUP_MISC | FLAG_RETURNS_FD },
        {"epoll_ctl",       5  | GROUP_MISC | FLAG_USES_FD },
        {"epoll_wait",      6  | GROUP_MISC | FLAG_USES_FD },
        {"eventfd",         7  | GROUP_MISC | FLAG_RETURNS_FD },
        {"eventfd2",        8  | GROUP_MISC | FLAG_RETURNS_FD },

        /** Network calls **/
        {"socket",          1  | GROUP_SOCKET | FLAG_RETURNS_FD },
        {"accept",          2  | GROUP_SOCKET | FLAG_USES_FD | FLAG_RETURNS_FD },
        {"connect",         3  | GROUP_SOCKET | FLAG_USES_FD },
        {"bind",            4  | GROUP_SOCKET | FLAG_USES_FD },
        {"listen",          5  | GROUP_SOCKET | FLAG_USES_FD },
        {"shutdown",        6  | GROUP_SOCKET | FLAG_USES_FD | FLAG_RELEASES_FD },
        {"getsockname",     7  | GROUP_SOCKET | FLAG_USES_FD },
        {"setsockopt",      8  | GROUP_SOCKET | FLAG_USES_FD },
        {"getsockopt",      9  | GROUP_SOCKET | FLAG_USES_FD },
        {"recv",            10 | GROUP_SOCKET | FLAG_USES_FD },
        {"recvmsg",         11 | GROUP_SOCKET | FLAG_USES_FD },
        {"recvfrom",        12 | GROUP_SOCKET | FLAG_USES_FD },
        {"send",            13 | GROUP_SOCKET | FLAG_USES_FD },
        {"sendmsg",         14 | GROUP_SOCKET | FLAG_USES_FD },
        {"sendto",          15 | GROUP_SOCKET | FLAG_USES_FD },

        /** Filesystem calls that involve FD **/
        {"open",            1  | GROUP_FILE | FLAG_RETURNS_FD | FLAG_HAS_STRING },
        {"creat",           2  | GROUP_FILE | FLAG_RETURNS_FD | FLAG_HAS_STRING },
        {"fstat",           3  | GROUP_FILE | FLAG_USES_FD },
        {"getdents",        4  | GROUP_FILE | FLAG_USES_FD },
        {"fsync",           5  | GROUP_FILE | FLAG_USES_FD },
        {"mkdirat",         6  | GROUP_FILE | FLAG_USES_FD | FLAG_HAS_STRING },
        {"linkat",          7  | GROUP_FILE | FLAG_USES_FD | FLAG_HAS_STRING | FLAG_SECOND_FD | FLAG_SECOND_STRING },
        {"symlinkat",       8  | GROUP_FILE | FLAG_USES_FD | FLAG_HAS_STRING | FLAG_SECOND_FD | FLAG_SECOND_STRING },
        {"futimesat",       9  | GROUP_FILE | FLAG_USES_FD | FLAG_HAS_STRING },
        {"fgetxattr",       10 | GROUP_FILE | FLAG_USES_FD },
        {"fsetxattr",       11 | GROUP_FILE | FLAG_USES_FD },
        {"fchmod",          12 | GROUP_FILE | FLAG_USES_FD },
        {"fchown",          13 | GROUP_FILE | FLAG_USES_FD },
        {"fchmodat",        14 | GROUP_FILE | FLAG_USES_FD | FLAG_HAS_STRING },
        {"openat",          15 | GROUP_FILE | FLAG_RETURNS_FD | FLAG_HAS_STRING },

        /** Filesystem calls that do not involve FD **/
        {"stat",           100 | GROUP_FILE | FLAG_HAS_STRING },
        {"lstat",          101 | GROUP_FILE | FLAG_HAS_STRING },
        {"access",         102 | GROUP_FILE | FLAG_HAS_STRING },
        {"unlink",         103 | GROUP_FILE | FLAG_HAS_STRING },
        {"getxattr",       104 | GROUP_FILE | FLAG_HAS_STRING },
        {"setxattr",       105 | GROUP_FILE | FLAG_HAS_STRING },
        {"mkdir",          106 | GROUP_FILE | FLAG_HAS_STRING },
        {"rmdir",          107 | GROUP_FILE | FLAG_HAS_STRING },
        {"chmod",          108 | GROUP_FILE | FLAG_HAS_STRING },
        {"mknod",          109 | GROUP_FILE | FLAG_HAS_STRING },
        {"rename",         110 | GROUP_FILE | FLAG_HAS_STRING | FLAG_SECOND_STRING },
        {"unlink",         110 | GROUP_FILE | FLAG_HAS_STRING  },
        {"link",           111 | GROUP_FILE | FLAG_HAS_STRING | FLAG_SECOND_STRING },
        {"symlink",        112 | GROUP_FILE | FLAG_HAS_STRING | FLAG_SECOND_STRING },
        {"chdir",          113 | GROUP_FILE | FLAG_HAS_STRING  },
        {"rename",         114 | GROUP_FILE | FLAG_HAS_STRING | FLAG_SECOND_STRING },
        {"readlink",       115 | GROUP_FILE | FLAG_HAS_STRING  },
        {"utime",          116 | GROUP_FILE | FLAG_HAS_STRING  },
        {"utimes",         117 | GROUP_FILE | FLAG_HAS_STRING  },
        {"chown",          118 | GROUP_FILE | FLAG_HAS_STRING  },
        {"chmod",          119 | GROUP_FILE | FLAG_HAS_STRING  },

        /** Memory calls **/
        {"mmap",            1  | GROUP_MEMORY },
        {"munmap",          2  | GROUP_MEMORY },
        {"mprotect",        3  | GROUP_MEMORY },
        {"futex",           4  | GROUP_MEMORY },
        {"mmap2",           5  | GROUP_MEMORY }

    };

    /*
     * Quickly lookup a call on the system table
     *
     * TODO This must be sorted and use sorted lookup
     *
     */
    private Integer lookupCall(String call) {
        for (Object o[] : syscallTable) {
            if (call.equals(o[0])) {
                return (Integer) o[1];
            }
        }
        return -1;
    }

    /*
     * SOCKET System Calls - FD Creation calls
     */
    private DynamicDescriptor handleCalls_socket_newFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {

        DynamicDescriptor newDD = dd;
        Long zFD1, zFD2;

        try {
            switch (callID) {

                case 1: /* Socket */
                    newDD = tracker.mapFD("Socket #"+returnValue.toString(), returnValue);
                    newDD.set("family", line.get("arg1"));
                    newDD.set("type", line.get("arg2"));
                    newDD.set("protocol", line.get("arg3"));                    
                    break;

                case 2: /* Accept */
                    newDD = tracker.mapFD("Socket #"+returnValue.toString(), returnValue);
                    newDD.set("family", dd.get("family"));
                    newDD.set("type", dd.get("type"));
                    newDD.set("protocol", dd.get("protocol"));
                    break;
                    
                case 203: /* dup2 */
                case 202: /* dup */
                    newDD = tracker.mapFD(dd, returnValue);
                    break;

                case 223: /* fcntl */
                    if ("F_DUPFD".equals(Str)) {
                        newDD = tracker.mapFD(dd, returnValue);
                    } else {
                        ErrorCollector.store_notice("Unhandled fcntl("+Str+")!");
                    }
                    break;

            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_socket_newFD", false);
        }
        
        return newDD;
    }

    /*
     * SOCKET System Calls - FD Management calls
     */
    private void handleCalls_socket_withFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        try {
            String sa_family,host,port;
            DynamicDescriptor newDD;
            switch (callID) {

                case 3: /* Connect */
                    dd.pileValue("time_connect", time);
                    sa_family = (String) line.get("=sa_family");
                    dd.set("remote_family", sa_family);
                    if (sa_family.equals("AF_FILE")) {
                        dd.set("remote", line.get("=path"));
                    } else if (sa_family.equals("AF_INET")) {
                        port = (String) (line.get("=sin_port", "htons(0)"));
                        host = (String) (line.get("=sin_addr", "inet_addr('0.0.0.0')"));
                        dd.set("remote",
                            host.subSequence(11, host.length() - 2) + ":"
                            + port.subSequence(6, port.length() - 1));
                    } else {
                        ErrorCollector.store_warning("Unknown address family: " +sa_family+" on line #"+line.lineID);
                    }
                    break;

                case 4: /* Bind */
                    dd.pileValue("time_wait", time);
                    sa_family = (String) line.get("=sa_family");
                    dd.set("remote_family", sa_family);
                    if (sa_family.equals("AF_FILE")) {
                        dd.set("local", line.get("=path"));
                    } else if (sa_family.equals("AF_INET")) {
                        port = (String) (line.get("=sin_port", "htons(0)"));
                        host = (String) (line.get("=sin_addr", "inet_addr('0.0.0.0')"));
                        dd.set("local",
                            host.subSequence(11, host.length() - 2) + ":"
                            + port.subSequence(6, port.length() - 1));
                    } else {
                        ErrorCollector.store_warning("Unknown address family: " +sa_family+" on line #"+line.lineID+ ". Network tracing can be affected for this socket");
                    }
                    break;

                case 5: /* listen */
                    dd.pileValue("time_wait", time);
                    break;

                case 13: /* send */
                case 14: /* sendmsg */
                case 15: /* sendto */
                case 204: /* write */
                case 205: /* pwrite */
                case 206: /* writev */
                    dd.pileValue("bytes_out", returnValue);
                    dd.pileValue("time_out", time);
                    toolBus.set("bytes_out", returnValue);
                    break;

                case 10: /* recv */
                case 11: /* recvmsg */
                case 12: /* recvfrom */
                case 207: /* read */
                case 208: /* pread */
                case 209: /* readv */
                    dd.pileValue("bytes_in", returnValue);
                    dd.pileValue("time_in", time);
                    toolBus.set("bytes_in", returnValue);
                    break;

            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_socket_withFD", false);
        }
    }

    /*
     * MEMORY System Calls - FD Creation calls
     */
    private DynamicDescriptor handleCalls_memory_newFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        DynamicDescriptor newDD = dd;
        return newDD;
    }

    /*
     * MEMORY System Calls - FD Management calls
     */
    private void handleCalls_memory_withFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
    }

    /*
     * MEMORY System Calls - FD Creation calls
     */
    private Integer lastPipeID = 0;
    private DynamicDescriptor handleCalls_misc_newFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        DynamicDescriptor newDD = dd;
        Double oldTime;
        Long zFD1,zFD2;
        
        try {
            switch (callID) {
                
                case 1: /* pipe */
                    lastPipeID++;

                    // Create first FD
                    newDD = tracker.mapFD("Pipe #"+lastPipeID.toString()+" " + FD.toString() + "<->" + FD2.toString(), FD);

                    // Initialize first FD (Since we can return only one FD)
                    newDD.set("worst_time", time);
                    newDD.set("worst_call", line.get("call"));
                    newDD.pileValue("time", time);
                    newDD.pileValue("calls", 1);

                    // Create second FD
                    newDD = tracker.mapFD("Pipe #"+lastPipeID.toString()+" " + FD.toString() + "<->" + FD2.toString(), FD2);
                    
                    // This one will be initialized after the return

                    break;

                case 3: /* socketpair */
                    lastPipeID++;
                    zFD1 = Long.parseLong((String)line.get("arg4",""));
                    zFD2 = Long.parseLong((String)line.get("arg5",""));

                    // Create first FD
                    newDD = tracker.mapFD("Socketpair #"+lastPipeID.toString()+" " + zFD1.toString() + "<->" + zFD2.toString(), zFD1);

                    // Initialize first FD (Since we can return only one FD)
                    newDD.set("worst_time", time);
                    newDD.set("worst_call", line.get("call"));
                    newDD.pileValue("time", time);
                    newDD.pileValue("calls", 1);
                    newDD.set("family", line.get("arg1"));
                    newDD.set("type", line.get("arg2"));
                    newDD.set("protocol", line.get("arg3"));

                    // Create second FD
                    newDD = tracker.mapFD("Socketpair #"+lastPipeID.toString()+" " + zFD1.toString() + "<->" + zFD2.toString(), zFD2);
                    newDD.set("family", line.get("arg1"));
                    newDD.set("type", line.get("arg2"));
                    newDD.set("protocol", line.get("arg3"));

                    // Second FD will be initialized upon exit

                    break;

                case 203: /* dup2 */
                case 202: /* dup */
                    newDD = tracker.mapFD(dd, returnValue);
                    break;
                    
                case 223: /* fcntl */
                    if ("F_DUPFD".equals(Str)) {
                        newDD = tracker.mapFD(dd, returnValue);
                    }
                    break;

            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_misc_newFD", false);
        }
        
        return newDD;
    }

    /*
     * MEMORY System Calls - FD Management calls
     */
    private void handleCalls_misc_withFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        
    }


    /*
     * FILESYSTEM System Calls - FD Creation calls
     */
    private DynamicDescriptor handleCalls_file_newFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        
        DynamicDescriptor newDD = dd;

        try {
            switch (callID) {

                case 1: /* Open */
                    newDD = tracker.mapFD(Str, returnValue);
                    newDD.set("file", Str);
                    newDD.set("flags", line.get("arg2", ""));
                    newDD.set("mode", line.get("arg3", ""));
                    break;

                case 2: /* Creat */
                    newDD = tracker.mapFD(Str, returnValue);
                    newDD.set("file", Str);
                    newDD.set("flags", line.get("arg2", ""));
                    if (returnValue == 4) {
                        returnValue = 4l;
                    }
                    break;

                case 203: /* dup2 */
                case 202: /* dup */
                    newDD = tracker.mapFD(dd, returnValue);
                    break;
                    
                case 223: /* fcntl */
                    if ("F_DUPFD".equals(Str)) {
                        newDD = tracker.mapFD(dd, returnValue);
                    }
                    break;

            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_memory_newFD", false);
        }

        return newDD;
    }

    /*
     * FILESYSTEM System Calls - FD Management calls
     */
    private void handleCalls_file_withFD(
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        try {
            switch (callID) {

                case 204: /* write */
                case 205: /* pwrite */
                case 206: /* writev */
                    dd.pileValue("bytes_out", returnValue);
                    dd.pileValue("time_out", time);
                    toolBus.set("bytes_out", returnValue);
                    break;

                case 207: /* read */
                case 208: /* pread */
                case 209: /* readv */
                    dd.pileValue("bytes_in", returnValue);
                    dd.pileValue("time_in", time);
                    toolBus.set("bytes_in", returnValue);
                    break;

                case 210: /* lseek */
                    dd.pileValue("time_seek", time);
                    break;
                    

            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_memory_newFD" ,false);
        }
    }

    /*
     * FILESYSTEM System Calls - FD Management calls
     */
    private void handleCalls_file_withFilename (
                TLineToken line, Integer callID, Long FD, Long FD2, String Str, String Str2, Long returnValue,
                FDTracker tracker, DynamicDescriptor dd, Double time
            ) {
        try {

        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.handleCalls_memory_newFD", false);
        }
    }

    /*
     * This is a template function used to split a big process
     * over the different call groups
     */
    static Integer hits = 0;
    private void branchAnalysis(
            Long pid, TLineToken line, String call, Integer callID, Integer CID, Long result, Long FD, Long FD2, String Str, String Str2
        ) {

        Integer class_int = 0; /* Used for speed: A local numeric index of the class */
        DynamicDescriptor dd = null;
        FDTracker fdt = null;
        Double time =0d, oldTime =0d;
        
        try {

            //######## BRANCHING #########//
            // Re-map the flags into an ID
            // in order to use switch { } structure
            if ((CID & GROUP_FILE)!=0) {
                class_int = 1;
            } else if ((CID & GROUP_SOCKET)!=0) {
                class_int = 2;
            } else  if ((CID & GROUP_MEMORY)!=0) {
                class_int = 3;
            } else  if ((CID & GROUP_MISC)!=0) {
                class_int = 4;
            } else {
                return;
            }
            //############################//

            // Does this systam call require a file descriptor?
            if ((CID & FLAG_USES_FD) !=0) {
                Boolean hasFD = false;

                //######## BRANCHING #########//
                // In case we are using manygroup syscall,
                // check for this PID on all stores...
                if (((CID & GROUP_FILE)!=0) && (hasFD == false)) {
                    fdt = getFileFDTracker(pid);
                    if (fdt.hasFD(FD)) {
                        toolBus.set("class", "file");
                        class_int = 1;
                        hasFD = true;
                    }
                }
                if (((CID & GROUP_SOCKET)!=0) && (hasFD == false)) {
                    fdt = getSocketFDTracker(pid);
                    if (fdt.hasFD(FD)) {
                        class_int = 2;
                        toolBus.set("class", "socket");
                        hasFD = true;
                    }
                }
                if (((CID & GROUP_MEMORY)!=0) && (hasFD == false)) {
                    fdt = getMemoryFDTracker(pid);
                    if (fdt.hasFD(FD)) {
                        class_int = 3;
                        toolBus.set("class", "memory");
                        hasFD = true;
                    }
                }
                if (((CID & GROUP_MISC)!=0) && (hasFD == false)) {
                    fdt = getMiscFDTracker(pid);
                    if (fdt.hasFD(FD)) {
                        class_int = 4;
                        toolBus.set("class", "misc");
                        hasFD = true;
                    }
                }
                // ^^ Up to here ---------------
                //##############################//

                // Do we have a known FD?
                if (hasFD) {

                    // We now know that this FD belongs to the group
                    // defined by the fdt variable. No we can safely
                    // calculate time, calls and failures
                    dd = fdt.getFD(FD);

                    time = (Double)line.get("time",0d);
                    oldTime = (Double)dd.get("worst_time", 0d);
                    if (time > oldTime) {
                        dd.set("worst_time", time);
                        dd.set("worst_call", call);
                    }

                    dd.pileValue("time", time);
                    dd.pileValue("calls", 1);

                    // Check failures
                    if ( result < 0 ) {
                        dd.pileValue("failures", 1);
                        dd.pileValue("failed_time", (Double)line.get("time", 0d));
                        dd.set("last_error", call + "() = "+ toolBus.get("error")); /** Look at the processLine() for the toolBus variable **/
                    }

                    /** Check FD release (We don't relly nead branch here) **/
                    if (( CID & FLAG_RELEASES_FD) != 0) {
                        fdt.releaseFD(FD);

                    /** Check for new FDs that also require an FD as an argument (like accept) **/
                    } else if ( ( ((CID & FLAG_RETURNS_FD) != 0) && (result > 0) ) || ((CID & FLAG_CREATES_FD2) != 0) ) {

                        // Error on FD creation? (The result > 0 check)
                        // We cannot map this thing anywhere!!

                        // TODO Are you sure? Again? 

                        //######## BRANCHING #########//
                        switch (class_int) {
                            case 1:
                                dd = handleCalls_file_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 2:
                                dd = handleCalls_socket_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 3:
                                dd = handleCalls_memory_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 4:
                                dd = handleCalls_misc_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                        }

                        if (dd == null) return;
                        time = (Double)line.get("time",0d);
                        oldTime = (Double)dd.get("worst_time", 0d);
                        if (time > oldTime) {
                            dd.set("worst_time", time);
                            dd.set("worst_call", call);
                        }
                        dd.pileValue("time", time);
                        dd.pileValue("calls", 1);
                        //############################//

                    /** Then forward the call to the normal FD call handling **/
                    } else {

                        //######## BRANCHING #########//
                        switch (class_int) {
                            case 1:
                                handleCalls_file_withFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 2:
                                handleCalls_socket_withFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 3:
                                handleCalls_memory_withFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                            case 4:
                                handleCalls_misc_withFD(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                                break;
                        }
                        //############################//

                    }

                /** FD Not found anywhere? **/
                } else {

                    // Raise an error for the rest
                    ErrorCollector.store_warning("Untraced file descriptor: "+FD.toString()+
                            "! Line #"+line.lineID.toString()+": "+line.fullLine+". An unimplemented system call could have created this fd");
                }

            // Does this system call create a file descriptor?
            } else if  ( ((CID & FLAG_CREATES_FD1) != 0) || ((CID & FLAG_CREATES_FD2) != 0) || ((CID & FLAG_RETURNS_FD) != 0) ) {
                
                // Create new FD
                if ( result < 0 ) {

                    // Error on FD creation?
                    // We cannot map this thing anywhere!!

                    // TODO Are you sure?
                    
                } else {

                    //######## BRANCHING #########//
                    switch (class_int) {
                        case 1:
                            time = (Double)line.get("time",0d);
                            fdt = getFileFDTracker(pid);
                            toolBus.set("class", "file");
                            dd = handleCalls_file_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, null, time);
                            break;
                        case 2:
                            time = (Double)line.get("time",0d);
                            fdt = getSocketFDTracker(pid);
                            toolBus.set("class", "socket");
                            dd = handleCalls_socket_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, null, time);
                            break;
                        case 3:
                            time = (Double)line.get("time",0d);
                            fdt = getMemoryFDTracker(pid);
                            toolBus.set("class", "memory");
                            dd = handleCalls_memory_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, null, time);
                            break;
                        case 4:
                            time = (Double)line.get("time",0d);
                            fdt = getMiscFDTracker(pid);
                            toolBus.set("class", "misc");
                            dd = handleCalls_misc_newFD(line, callID, FD, FD2, Str, Str2, result, fdt, null, time);
                            break;
                    }
                    
                    if (dd == null) return;
                    oldTime = (Double)dd.get("worst_time", 0d);
                    if (time > oldTime) {
                        dd.set("worst_time", time);
                        dd.set("worst_call", call);
                    }
                    dd.pileValue("time", time);
                    dd.pileValue("calls", 1);
                    //############################//

                }

            // Does this systam call require a string parameter?
            } else if ((CID & FLAG_HAS_STRING) !=0) {

                //######## BRANCHING #########//
                if ((CID & GROUP_FILE)!=0) {
                    toolBus.set("class", "file");
                    fdt = getFileFDTracker(pid);
                    dd = fdt.findDescriptor(Str, Boolean.TRUE);

                    time = (Double)line.get("time",0d);
                    oldTime = (Double)dd.get("worst_time", 0d);
                    if (time > oldTime) {
                        dd.set("worst_time", time);
                        dd.set("worst_call", call);
                    }
                    dd.pileValue("time", time);
                    dd.pileValue("calls", 1);
                    dd.set("file", Str);
                    
                    if ( result < 0 ) {
                        dd.pileValue("failures", 1);
                        dd.pileValue("time", (Double)line.get("time", 0d));
                        dd.set("last_error", call + "() = "+toolBus.get("error")); /** Look at the processLine() for the toolBus variable **/
                    } else {
                        handleCalls_file_withFilename(line, callID, FD, FD2, Str, Str2, result, fdt, dd, time);
                    }
                } else if ((CID & GROUP_SOCKET)!=0) {
                    toolBus.set("class", "socket");
                } else if ((CID & GROUP_MEMORY)!=0) {
                    toolBus.set("class", "memory");
                } else if ((CID & GROUP_MISC)!=0) {
                    toolBus.set("class", "misc");
                }
                //############################//

            } 
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.branchAnalysis", false);
        }

    }

    private void cloneAllPIDDescriptors(Long pid, Long childPID) {
        FDTracker srcFD;
        if (fileFD.containsKey(pid)) {
            srcFD = new FDTracker(fileFD.get(pid), false);
            fileFD.put(childPID, srcFD);
        }
        if (socketFD.containsKey(pid)) {
            srcFD = new FDTracker(socketFD.get(pid), false);
            socketFD.put(childPID, srcFD);
        }
        if (memoryFD.containsKey(pid)) {
            srcFD = new FDTracker(memoryFD.get(pid), false);
            memoryFD.put(childPID, srcFD);
        }
        if (miscFD.containsKey(pid)) {
            srcFD = new FDTracker(miscFD.get(pid), false);
            miscFD.put(childPID, srcFD);
        }
    }

    private ArrayList<Long> getSelectFDs(TLineToken line) {
        ArrayList<Long> list = new ArrayList<Long>();

        // The first one is the highest FD + 1
        Integer i = 2;
        Long fd;

        // The rest are the FDs in array format [x, x], [], [x, x, x]
        Object arg = line.get("arg"+i.toString());
        while (arg != null) {
            fd = (Long)arg;
            list.add(fd);
            arg = line.get("arg"+i.toString());
            i++;
        }

        // Last two is the timeout structure {x, X}
        list.remove(list.size()-1);
        list.remove(list.size()-1);
        return list;
    }

    @Override
    public void processLine(TLineToken line) {
        //System.out.print(line.fullLine+"\n");
        try {

            // Really optimized for speed!
            //
            // - Minimum calls to line.get()
            // - Maximum available info passed to handler
            //
            Object returnValue = line.get("return", 0l);
            String call = (String)line.get("call");
            Integer CID = lookupCall(call);
            Long pid = (Long)line.get("pid",0l);
            Long result = 0l;
            Double time = (Double)line.get("time",0d);
            if (returnValue.getClass() == Long.class) { /* Result might also be string! */
                result = (Long)returnValue;
            }

            // Store the accumulated time
            totalTime += time;

            // In case of error, dependless the kind of the
            // system call, save the error on the error stack
            if (result < 0) {
                toolBus.set("error", result);
                if (line.containsKey("return_desc")) {
                    String error, errorDesc;

                    String parts[] = ((String)line.get("return_desc", "UNKNOWN (Unknown error)")).trim().split(" ",2);
                    error = parts[0];
                    errorDesc = parts[1].substring(1, parts[1].length()-1);

                    // Store info on toolBus
                    toolBus.set("error", error);
                    if (parts.length > 1) {
                        toolBus.set("error_desc", errorDesc);
                    }

                    // Update local store
                    IOErrors IOerror;
                    if (errorList.containsKey(error)) {
                        IOerror = errorList.get(error);
                    } else {
                        IOerror = new IOErrors();
                        IOerror.code = result;
                        IOerror.hits = 1;
                        IOerror.shorName = error;
                        IOerror.longName = errorDesc;
                        IOerror.totalTime = 0.00;
                        errorList.put(error, IOerror);
                    }
                    IOerror.totalTime += time;
                    IOerror.hits++;

                }
            }

            if (CID < 0) {
                if (!unknownCalls.contains(call)) {
                    unknownCalls.add(call);
                }
                return;

            } else {
                
                // Fetch commonly used fields
                Integer callID = CID & MASK_ID;

                //System.out.print("     Handling call: "+call+" with ID="+callID.toString()+" ARG1="+line.get("arg1","")+" ARG2="+line.get("arg2","")+" > "+result.toString()+"\n");

                // Based on the flags, fetch what's needed
                Long FD = 0l;
                Long FD2 = 0l;
                Long ret = 0l;
                String Str = "";
                String Str2 = "";
                if (((CID & FLAG_USES_FD) != 0) || ((CID & FLAG_CREATES_FD1) != 0)) {
                    // call(fd);
                    FD = (Long)line.get("arg1", 0l);
                    if ((CID & FLAG_HAS_STRING) != 0) {
                        // call(fd, "str");
                        Str = (String)line.get("arg2", "");
                        if ((CID & FLAG_SECOND_FD) != 0) {
                            // call(fd, "str", fd, "str");
                            FD2 = (Long)line.get("arg3", 0l);
                            if ((CID & FLAG_SECOND_STRING) != 0) {
                                Str2 = (String)line.get("arg4","");
                            }
                        }
                    } else if (((CID & FLAG_SECOND_FD) != 0) || ((CID & FLAG_CREATES_FD2) != 0)) {
                        // call(fd, fd)
                        FD2 = (Long)line.get("arg2", 0l);
                    }
                } else if ((CID & FLAG_HAS_STRING) != 0) {
                    // call("st")
                    Str = (String)line.get("arg1", "");
                    if ((CID & FLAG_SECOND_STRING) != 0) {
                        // call("str", "str");
                        Str2 = (String)line.get("arg2", "");
                    }
                }

                // Check for special system calls
                if ((CID & GROUP_SPECIAL) != 0) {

                    switch (callID) {

                        case 1: /* clone */
                            String flags = (String)line.get("=flags", "");
                            if (flags.contains("CLONE_FILES")) {
                                // Clone files = The child will share the same FDs with
                                //               the parent. So, just add the child on the
                                //               clone mapping table.
                                cloneMapping.put(result, pid);
                            } else {
                                // Otherways, the child will receive a COPY of the file descriptors
                                // So, clone this process
                                cloneAllPIDDescriptors(pid, result);
                            }
                            break;

                        case 2: /* select */

                            // Fetch the FDs from the select statement
                            ArrayList<Long> fds = getSelectFDs(line);
                            FDTracker fdt1, fdt2, fdt3, fdt4;
                            DynamicDescriptor dd;
                            Double oldTime;

                            // For each FD find it's tracker
                            fdt1 = getFileFDTracker(pid);
                            fdt2 = getMemoryFDTracker(pid);
                            fdt3 = getMiscFDTracker(pid);
                            fdt4 = getSocketFDTracker(pid);
                            
                            for (Long fd: fds) {
                                dd = null;
                                if (fdt1.hasFD(fd)) {
                                    dd = fdt1.getFD(fd);
                                }
                                if (fdt2.hasFD(fd)) {
                                    dd = fdt2.getFD(fd);
                                }
                                if (fdt3.hasFD(fd)) {
                                    dd = fdt3.getFD(fd);
                                }
                                if (fdt4.hasFD(fd)) {
                                    dd = fdt4.getFD(fd);
                                }

                                // If we have found the FD
                                // stack the info
                                if (dd != null) {
                                    oldTime = (Double)dd.get("worst_time", 0d);
                                    if (time > oldTime) {
                                        dd.set("worst_time", time);
                                        dd.set("worst_call", "select");
                                    }
                                    dd.pileValue("time", time);
                                    dd.pileValue("calls", 1);
                                } else {
                                    ErrorCollector.store_warning("Untraced file descriptor: "+fd.toString()+
                                            "! Line #"+line.lineID.toString()+": "+line.fullLine+". An unimplemented system call could have created this fd");
                                }
                            }

                            break;

                    }

                // Fortward the rest to the branched analysis
                } else {
                    branchAnalysis(pid, line, call, callID, CID, result, FD, FD2, Str, Str2);
                }
                
            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "LCallClassifier.processLine", false);
        }

    }

    @Override
    public void beginAnalysis() {
        //
    }

    private String formatBytes(Long bytes) {
        Float f;
        if (bytes < 1024) {
            return String.format("%d b", bytes);
        } else {
            f = bytes.floatValue()/1024;
            if (f < 1024) {
                return String.format("%.2f Kb", f);
            } else {
                f = f/1024;
                if (f < 1024) {
                    return String.format("%.2f Mb", f);
                } else {
                    f = f/1024;
                    return String.format("%.2f Gb", f);
                }
            }
        }
    }

    private String formatTime(Double microTime) {
        return String.format("%.4f ms", microTime * 1000);
    }

    private void storeSummary(String group, IOSummary summary ) {
        presenter.gridSummary.addRow(new Object[] { group, "Total bytes In", formatBytes(summary.totalByesIn) });
        presenter.gridSummary.addRow(new Object[] { group, "Total bytes Out", formatBytes(summary.totalByesOut) });
        presenter.gridSummary.addRow(new Object[] { group, "Total file descriptors", String.valueOf(summary.totalFDs) });
        presenter.gridSummary.addRow(new Object[] { group, "Total system calls", String.valueOf(summary.totalHits) });
        presenter.gridSummary.addRow(new Object[] { group, "Total time spent", formatTime(summary.totalTime) });
        presenter.gridSummary.addRow(new Object[] { group, "Total failures", String.valueOf(summary.totalFailures) });
        presenter.gridSummary.addRow(new Object[] { group, "Total failure time", formatTime(summary.totalFailedTime) });
        presenter.gridSummary.addRow(new Object[] { group, "Worst instruction", summary.worstInstruction });
        presenter.gridSummary.addRow(new Object[] {  group, "Worst time", formatTime(summary.worstTime) });
    }

    @Override
    public void completeAnalysis() {
        // Archive the remaining FDs
        for (Long pid: socketFD.keySet()) {
            socketFD.get(pid).archiveRemaining();
        }
        for (Long pid: fileFD.keySet()) {
            fileFD.get(pid).archiveRemaining();
        }
        for (Long pid: memoryFD.keySet()) {
            memoryFD.get(pid).archiveRemaining();
        }

        // Prepare the summary
        IOSummary summary = new IOSummary();

        // Display the results
        FDTracker fdt;
        for (Long pid: socketFD.keySet()) {
            fdt = socketFD.get(pid);
            for ( FDTracker.DynamicDescriptor desc: fdt.archive ) {
                summary.totalFDs += 1;
                summary.totalByesIn += (Long)desc.get("bytes_in", 0l);
                summary.totalByesOut += (Long)desc.get("bytes_out", 0l);
                summary.totalHits += (Integer)desc.get("calls", 0);
                summary.totalTime += (Double)desc.get("time", 0.0);
                summary.totalFailures += (Integer)desc.get("failures", 0);
                summary.totalFailedTime += (Double)desc.get("failed_time", 0.0);
                summary.worstInstruction = (String)desc.get("worst_call", "");
                summary.worstTime = (Double)desc.get("worst_time", 0d);

                presenter.gridSockets.addRow(new Object[] {
                    pid.toString() + " " + (String)router.queryTools("pid_name", new Object[] { pid }),
                    desc.get("local", ""), desc.get("remote", ""),
                    desc.get("family", "UNKNOWN"), desc.get("type", "UNKNOWN"),
                    desc.get("calls", 0),(Double) desc.get("time", 0d) * 1000,
                    desc.get("failures", 0), (Double)desc.get("failed_time", 0d ) * 1000,
                    desc.get("bytes_out", 0l), desc.get("bytes_in", 0l),
                    desc.get("last_error", "")
                });
            }
        }
        storeSummary("Network Sockets", summary);

        summary = new IOSummary();
        for (Long pid: fileFD.keySet()) {
            fdt = fileFD.get(pid);
            for ( FDTracker.DynamicDescriptor desc: fdt.archive ) {
                summary.totalFDs += 1;
                summary.totalByesIn += (Long)desc.get("bytes_in", 0l);
                summary.totalByesOut += (Long)desc.get("bytes_out", 0l);
                summary.totalHits += (Integer)desc.get("calls", 0);
                summary.totalTime += (Double)desc.get("time", 0.0);
                summary.totalFailures += (Integer)desc.get("failures", 0);
                summary.totalFailedTime += (Double)desc.get("failed_time", 0.0);
                summary.worstInstruction = (String)desc.get("worst_call", "");
                summary.worstTime = (Double)desc.get("worst_time", 0.0);

                presenter.gridFilesystem.addRow(new Object[] {
                    pid.toString() + " " + (String)router.queryTools("pid_name", new Object[] { pid }),
                    desc.get("file","(Unknown)"),
                    desc.get("calls", 0), (Double)desc.get("time", 0d) * 1000,
                    desc.get("failures", 0), (Double)desc.get("failed_time", 0d) * 1000,
                    desc.get("bytes_out", 0l), desc.get("bytes_in", 0l),
                    desc.get("last_error", "")
                });
            }
        }
        storeSummary("Filesystem", summary);

        summary = new IOSummary();
        for (Long pid: miscFD.keySet()) {
            fdt = miscFD.get(pid);
            for ( FDTracker.DynamicDescriptor desc: fdt.archive ) {
                summary.totalFDs += 1;
                summary.totalByesIn += (Long)desc.get("bytes_in", 0l);
                summary.totalByesOut += (Long)desc.get("bytes_out", 0l);
                summary.totalHits += (Integer)desc.get("calls", 0);
                summary.totalTime += (Double)desc.get("time", 0.0);
                summary.totalFailures += (Integer)desc.get("failures", 0);
                summary.totalFailedTime += (Double)desc.get("failed_time", 0.0);
                summary.worstInstruction = (String)desc.get("worst_call", "");
                summary.worstTime = (Double)desc.get("worst_time", 0.0);

                presenter.gridMisc.addRow(new Object[] {
                    pid.toString() + " " + (String)router.queryTools("pid_name", new Object[] { pid }),
                    desc.index,
                    desc.get("calls", 0), (Double)desc.get("time", 0d) * 1000,
                    desc.get("failures", 0), (Double)desc.get("failed_time", 0d) * 1000,
                    desc.get("bytes_out", 0l), desc.get("bytes_in", 0l),
                    desc.get("last_error", "")
                });
            }
        }
        storeSummary("Misc", summary);

        // Store the unknown calls
        for (String key: unknownCalls) {
            presenter.listUnhandled.addLine(key);
        }

        // Store the errors
        for (IOErrors err: errorList.values()) {
            presenter.gridFailures.addRow(new Object[] {
                err.shorName, err.longName,
                err.hits, err.totalTime * 1000
            });
        }
    }

    @Override
    public void reset() {
        socketFD.clear();
        fileFD.clear();
        memoryFD.clear();
        presenter.reset();
    }

    @Override
    public Object getInformation(String parameter, Object[] arguments) {
        if ("fd_socket".matches(parameter)) return socketFD;
        if ("fd_file".matches(parameter)) return fileFD;
        if ("fd_memory".matches(parameter)) return memoryFD;
        if ("fd_unknown".matches(parameter)) return unknownCalls;
        if ("call_time".matches(parameter)) return totalTime;
        return null;
    }

    @Override
    public TPresenter getPresenter() {
        return presenter;
    }

}

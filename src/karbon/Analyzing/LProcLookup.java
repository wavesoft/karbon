/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.TPreprocessingTool;
import KAnalyzer.Interfaces.TLineToken;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author Ioannis Charalampidis
 */
public class LProcLookup extends TPreprocessingTool {

    private class ProcInfo {
        public String imageName;
        public String arguments;
        public Long parent;
        public Long pid;
        public Double timeStarted;
        public Double timeFinished;
    }

    private Double timeStarted = -1d;
    private Double timeFinished = 0d;
    Hashtable<Long, ProcInfo> pidInfo = new Hashtable<Long, ProcInfo>();

    @Override
    public Object getInformation(String parameter, Object[] arguments) {
        if ("pid_name".equals(parameter)) {
            Long pid = (Long)arguments[0];
            if (pidInfo.containsKey(pid)) {
                ProcInfo info = pidInfo.get(pid);
                return info.imageName;
            }
        } else if ("pid_arguments".equals(parameter)) {
            Long pid = (Long)arguments[0];
            if (pidInfo.containsKey(pid)) {
                ProcInfo info = pidInfo.get(pid);
                return info.arguments;
            }
        } else if ("pid_time".equals(parameter)) {
            Long pid = (Long)arguments[0];
            if (pidInfo.containsKey(pid)) {
                ProcInfo info = pidInfo.get(pid);
                return info.timeFinished - info.timeStarted;
            }
        } else if ("pid_parent".equals(parameter)) {
            Long pid = (Long)arguments[0];
            if (pidInfo.containsKey(pid)) {
                ProcInfo info = pidInfo.get(pid);
                return info.parent;
            }
        } else if ("pid_list".equals(parameter)) {
            ArrayList<Object[]> pidList = new ArrayList<Object[]>();
            for (ProcInfo p: pidInfo.values()) {
                pidList.add(new Object[] { 
                    p.pid, p.parent, p.imageName, p.arguments, p.timeStarted, p.timeFinished
                });
            }
        } else if ("total_time".equals(parameter)) {
            return timeFinished - timeStarted;
        }
        return null;
    }

    private String extractArgs(TLineToken t) {
        Integer i = 3;
        String s = "";
        while ( t.containsKey("arg" + i.toString()) ) {
            s += " "+ (String)t.get("arg" + i.toString());
            i++;
        }
        return s;
    }

    @Override
    public void processLine(TLineToken line) {
        String call = (String) line.get("call","");
        Double timestamp = (Double)line.get("timestamp", 0d);
        Long pid = (Long) line.get("pid", 0l);
        ProcInfo info = null;

        // Initialize started and finished timestamp
        if (timeStarted < 0) timeStarted = timestamp;
        timeFinished = timestamp;
        
        // Try to map PIDs to file names
        if (call.equals("execve")) {
            info = new ProcInfo();
            info.imageName = (String)line.get("arg1", "");
            info.arguments = extractArgs(line);
            info.pid = pid;
            info.timeStarted = timestamp;
            pidInfo.put(pid, info);
        } else if (call.equals("getppid")) {
            if (pidInfo.containsKey(pid)) {
                pidInfo.get(pid).parent = (Long)line.get("return");
            }
        } else if (call.equals("clone") || call.equals("fork") || call.equals("vfork")) {
            Long child = (Long)line.get("return");
            if (pidInfo.containsKey(pid)) {
                ProcInfo subinfo = new ProcInfo();
                info = pidInfo.get(pid);
                subinfo.imageName = info.imageName + " - clone";
                subinfo.arguments = info.arguments;
                subinfo.parent = pid;
                subinfo.pid = child;
                info.timeStarted = timestamp;
                pidInfo.put(child, info);
            }
        }

        // Update timestamps
        if (info == null) {
            if (pidInfo.containsKey(pid)) {
                info = pidInfo.get(pid);
            } else {
                info = new ProcInfo();
                info.timeStarted = timestamp;
            }
            info.timeFinished = timestamp;
        }
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {
    }

    @Override
    public void reset() {
        pidInfo.clear();
    }
    
}

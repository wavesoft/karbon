/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.API.TPresenter;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Utils.FDTracker;
import KAnalyzer.Utils.FDTracker.DynamicDescriptor;
import java.util.Hashtable;

/**
 *
 * Hotspot tracking analyzer
 * 
 * This class traces hot spots on system call,
 * filesystem and processes.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class KHotspotTracker extends TAnalyzeTarget {

    private class FileInfo {
        public Double time = 0d;
        public Double failedTime = 0d;
        public String name = "";
    }

    private class CallInfo {
        public Double time = 0d;
        public Double failedTime = 0d;
        public String name = "";
    }

    private class PIDInfo {
        public Double pidTime = 0d;
        public Double pidFailedTime = 0d;
        public Long pid = 0l;
    }
    
    private Hashtable<String, CallInfo> hsCalls;
    private Hashtable<Long, PIDInfo> hsPIDs;
    private Double totalTime = 0d;
    private Double maxTime = 0d;
    private Double minTime = -1d;
    private PHotspotPresenter presenter;

    public KHotspotTracker() {
        hsCalls = new Hashtable<String, CallInfo>();
        hsPIDs = new Hashtable<Long, PIDInfo>();

        presenter = new PHotspotPresenter();
    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
        String call = (String)line.get("call");
        Long pid = (Long)line.get("pid");
        Double callTime = (Double)line.get("time");
        Double timeStamp = (Double)line.get("timestamp");
        Object callClass = toolBus.get("class");
        Object callError = toolBus.get("error");

        // Calculate time range
        if (minTime < 0) {
            minTime = timeStamp;
        } else {
            if (timeStamp < minTime) minTime = timeStamp;
        }
        if (timeStamp > maxTime) maxTime = timeStamp;

        // Calculate total time
        totalTime += callTime;

        // Implement the call/failed call hotspot detector
        CallInfo cInfo;
        if (hsCalls.containsKey(call)) {
            cInfo = hsCalls.get(call);
        } else {
            cInfo = new CallInfo();
            cInfo.name = call;
            hsCalls.put(call, cInfo);
        }
        cInfo.time += callTime;
        if (callError != null) {
            cInfo.failedTime += callTime;
        }

        // Implement PID time calculation
        PIDInfo pInfo;
        if (hsPIDs.containsKey(pid)) {
            pInfo = hsPIDs.get(pid);
        } else {
            pInfo = new PIDInfo();
            pInfo.pid = pid;
            hsPIDs.put(pid, pInfo);
        }
        pInfo.pidTime += callTime;
        if (callError != null) {
            pInfo.pidFailedTime += callTime;
        }

    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {

        Double totalDuration = maxTime - minTime;

        for (CallInfo info: hsCalls.values()) {
            presenter.gridCalls.addRow(new Object[] {
                info.name,
                100 * (info.time - info.failedTime) / totalTime,
                100 * info.failedTime / totalTime,
                100 * info.time / totalTime
            });
        }
        for (PIDInfo info: hsPIDs.values()) {
            presenter.gridProccess.addRow(new Object[] {
                info.pid,
                router.queryTools("pid_name", new Object[] { info.pid }),
                router.queryTools("pid_arguments", new Object[] { info.pid }),
                100 * (info.pidTime - info.pidFailedTime) / totalDuration,
                100 * info.pidFailedTime / totalDuration,
                100 * info.pidTime / totalDuration
            });
        }

        Hashtable<String, FileInfo> fileInfo = new Hashtable<String, FileInfo>();
        Hashtable<Long, FDTracker> files = (Hashtable<Long, FDTracker>)router.queryTools("fd_file", null);
        FDTracker tracker;
        FileInfo fInfo;

        // Loop over the PIDs that used this file
        for (Long pid: files.keySet()) {
            tracker = files.get(pid);

            // Loop over the archived FD actions
            for (DynamicDescriptor dd: tracker.archive) {

                // Get/Create file entry
                if (fileInfo.containsKey(dd.index)) {
                    fInfo = fileInfo.get(dd.index);
                } else {
                    fInfo = new FileInfo();
                    fInfo.name = dd.index;
                    fileInfo.put(dd.index, fInfo);
                }

                // Apped time info
                fInfo.time += (Double)dd.get("time",0d);
                fInfo.failedTime += (Double)dd.get("failed_time", 0d);

            }
        }

        // Store the file info
        for (FileInfo info: fileInfo.values()) {
            presenter.gridFilesystem.addRow(new Object[] {
                info.name,
                100 * (info.time - info.failedTime) / totalTime,
                100 * info.failedTime / totalTime,
                100 * info.time / totalTime
            });
        }

    }

    @Override
    public void reset() {
        presenter.reset();
        totalTime = 0d;
        hsCalls.clear();
        hsPIDs.clear();
    }

    @Override
    public TPresenter getPresenter() {
        return presenter;
    }

}

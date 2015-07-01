/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;
import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.API.TPresenter;
import KAnalyzer.ErrorCollector;
import KAnalyzer.Interfaces.TToolBus;
import KAnalyzer.Utils.FDTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class KProcessTracker extends TAnalyzeTarget {

    public static final Integer LOG_MAX_HITS = 1000;
    public static final Double LOG_HIT_DIFFERENCE = 1.0; // Time step in seconds

    public class ProcessInfo {
        public Double started = 0.00;
        public Double stopped = 0.00;
        public Long pid = 0L;
        public String imageName = "";
        public String arguments = "";
        public Long parent = 0L;
        public Integer hits[];

        public ProcessInfo() {
            hits = new Integer[LOG_MAX_HITS];
            for (int i=0; i<LOG_MAX_HITS; i++) {
                hits[i] = 0;
            }
        }
    }

    FDTracker fd = new FDTracker();
    PProcessPresenter presenter;
    Long rootPID = -1L;
    Double maxTime = 0D;
    Double minTime = -1D;

    Hashtable<Long, ProcessInfo> procmap = new Hashtable<Long, ProcessInfo>();
    ArrayList<Long> handled = new ArrayList<Long>();

    public KProcessTracker() {
        presenter = new PProcessPresenter();
        presenter.Title = "Process Graph";
    }

    @Override
    public void reset() {
        presenter.reset();
        procmap = new Hashtable<Long, ProcessInfo>();
        handled = new ArrayList<Long>();
        rootPID = -1L;
        maxTime = 0D;
        minTime = -1D;
    }

    private void buildTreeModel(Long pid, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode node;
        handled.add(pid);
        for (ProcessInfo inf: procmap.values()) {
            if (inf.parent.equals(pid)) {
                node = new DefaultMutableTreeNode("(" + inf.parent.toString() + " -> " + inf.pid.toString() + ") "+ inf.imageName);
                parent.add(node);
                buildTreeModel(inf.pid, node);
            }
        }
    }

    private void buildOrphanTreeModel(DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode node;
        for (ProcessInfo inf: procmap.values()) {
            if (!handled.contains(inf.pid)) {
                node = new DefaultMutableTreeNode("(" + inf.parent.toString() + " -> " + inf.pid.toString() + ") "+ inf.imageName);
                parent.add(node);
                buildTreeModel(inf.pid, parent);
            }
        }
    }

    private ProcessInfo getProcInfo(Long pid, Double timestamp) {
        ProcessInfo pinfo;
        if (!procmap.containsKey(pid)) {
            pinfo = new ProcessInfo();
            pinfo.started = timestamp;
            pinfo.pid = pid;
        } else {
            pinfo = procmap.get(pid);
        }
        return pinfo;
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
    public void receiveLine(TLineToken line, IAnalyzeRule rule) {
        try {
            String call = (String) line.get("call");
            Double timestamp = (Double) line.get("timestamp");
            Long pid = (Long) line.get("pid");
            if (rootPID < 0) rootPID = pid;
            if (minTime < 0) minTime = timestamp;
            ProcessInfo pinfo;

            // Create procmap key it does  not exist
            pinfo = getProcInfo(pid, timestamp);

            // Try to map PIDs to file names
            if (call.equals("execve")) {
                pinfo.imageName = (String)line.get("arg1");
                pinfo.arguments = extractArgs(line);
                presenter.AddPIDMap((Long)line.get("pid"), pinfo.imageName, pinfo.arguments);
            } else if (call.equals("getppid")) {
                pinfo.parent = (Long)line.get("return");
            } else if (call.equals("clone") || call.equals("fork") || call.equals("vfork")) {
                Long child = (Long)line.get("return");
                ProcessInfo subinfo = getProcInfo(child, timestamp);
                subinfo.parent = pid;
                subinfo.arguments = pinfo.arguments;
                subinfo.imageName = pinfo.imageName + " (" + call  + ")";
                presenter.AddPIDMap((Long)line.get("pid"), pinfo.imageName, pinfo.arguments);
                procmap.put(child, subinfo);
            }

            // Store traffic info on the PID
            Integer slot = ((Double)((timestamp - minTime) / LOG_HIT_DIFFERENCE)).intValue();
            pinfo.hits[slot]++;

            // Update stop time (So if we don't see this again
            // it means it expired)
            pinfo.stopped = timestamp;
            if (timestamp > maxTime) maxTime = timestamp;

            // Store PID info
            procmap.put(pid, pinfo);
            
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "KProcessTracker.receveLine", false);
        }
    }

    @Override
    public TPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {

        // Create the graph information
        DefaultMutableTreeNode rootNode, orphanNode, pidNode;
        rootNode = new DefaultMutableTreeNode("PID Tree");
        pidNode = new DefaultMutableTreeNode("(" + rootPID.toString()  + ") Root");
        orphanNode = new DefaultMutableTreeNode("Orphan PIDs");
        
        buildTreeModel(rootPID, pidNode);
        buildOrphanTreeModel(orphanNode);

        rootNode.add(pidNode);
        rootNode.add(orphanNode);

        DefaultTreeModel m = new DefaultTreeModel(rootNode);
        presenter.setTreeModel(m);

        ArrayList<ProcessInfo> processes = new ArrayList<ProcessInfo>();
        for (ProcessInfo pinfo: procmap.values() ) {
            processes.add(pinfo);
        }
        Collections.sort(processes, new PIDComparator());
        presenter.procInfo = processes;

        presenter.procMaxTime = maxTime;
        presenter.procMinTime = minTime;
        presenter.RedrawGraph();
        
    }

}

class PIDComparator implements Comparator<KProcessTracker.ProcessInfo>{

    @Override
    public int compare(KProcessTracker.ProcessInfo p1, KProcessTracker.ProcessInfo p2) {

        Long rank1 = p1.pid;
        Long rank2 = p2.pid;

        if (rank1 > rank2){
            return +1;
        }else if (rank1 < rank2){
            return -1;
        }else{
            return 0;
        }
    }
}

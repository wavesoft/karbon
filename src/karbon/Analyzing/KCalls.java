/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.API.TPresenter;
import KAnalyzer.Interfaces.TToolBus;
import java.util.Hashtable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class KCalls extends TAnalyzeTarget {

    private PCallsPresenter myPresenter;

    private static class CallInfo {
        Long hits = 0l;
        Double time = 0d;
        Long errors = 0l;
        Double errorTime = 0d;
        String name = "";
    }

    private Hashtable<Long, Hashtable<String, CallInfo>> callMap = new Hashtable<Long, Hashtable<String, CallInfo>>();

    public KCalls() {
        this.createPresenter();
        myPresenter.target = this;
        myPresenter.Title = "System Calls";
        
        myPresenter.gridGeneralList.setColumnInfo(new Object[][] {
            {"System Call", String.class, 500},
            {"Hits", Long.class, 300},
            {"Errors", Long.class, 300},
            {"Time (ms)", Double.class, 300},
            {"Error Time (ms)", Double.class, 300},
            {"Average Time (ms)", Double.class, 300}
        });

        myPresenter.gridPIDDetails.setColumnInfo(new Object[][] {
            {"System Call", String.class, 500},
            {"Hits", Long.class, 300},
            {"Errors", Long.class, 300},
            {"Time (ms)", Double.class, 300},
            {"Error Time (ms)", Double.class, 300},
            {"Average Time (ms)", Double.class, 300}
        });

        myPresenter.gridPIDList.setColumnInfo(new Object[][] {
            {"PID", Long.class, 100},
            {"Image Name", String.class, 400},
            {"Calls", Long.class, 100},
            {"Errors", Long.class, 100},
            {"Total Time", Double.class, 100},
            {"Average Time", Double.class, 100}
        });


        myPresenter.gridPIDList.jGridData.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Integer row = myPresenter.gridPIDList.jGridData.getSelectedRow();
                Long pid = 0l;
                DefaultTableModel model = (DefaultTableModel) myPresenter.gridPIDList.jGridData.getModel();
                pid = (Long)model.getValueAt(row, 0);
                myPresenter.gridPIDDetails.Title = "System call details of PID "+pid.toString()+" ("+router.queryTools("pid_name", new Object[] { pid })+")";
                showRowDetails(pid);
            }
        });

        /*
        myPresenter.gridPIDList.jGridData.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Integer row = myPresenter.gridPIDList.jGridData.getSelectedRow();
                showRowDetails(row);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //
            }
        });
         */

    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
        String call = (String) line.get("call");
        call = call.trim().replace("\t", "").replace("\r", "").replace("\n", "");
        Long pid = (Long)line.get("pid", 0l);

        // Fetch the call table
        Hashtable<String, CallInfo> calls = null;
        if (!callMap.containsKey(pid)) {
            calls = new Hashtable<String, CallInfo>();
            callMap.put(pid, calls);
        } else {
            calls = callMap.get(pid);
        }

        // Fetch/create call info
        CallInfo info;
        if (!calls.containsKey(call)) {
            info = new CallInfo();
            info.name = call;
            calls.put(call, info);
        } else {
            info = calls.get(call);
        }

        // LCallClassifier (Preprocessing Tool)
        // Provides error detection
        Double time = (Double) line.get("time");
        if (toolBus.containsKey("error")) {
            info.errors++;
            info.errorTime += time;
        }

        // Store hit
        info.hits++;
        info.time += time;
        calls.put(call, info);
    }

    private void createPresenter() {
        myPresenter = new PCallsPresenter();
    }

    @Override
    public TPresenter getPresenter() {
        return myPresenter;
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {
        Hashtable<String, CallInfo> summary = new Hashtable<String, CallInfo>();
        Hashtable<String, CallInfo> entry;
        CallInfo info, localSum;
        String pidName;

        // Calculate the summary
        for (Long pid: callMap.keySet()) {
            entry = callMap.get(pid);
            localSum = new CallInfo();
            for (CallInfo e: entry.values()) {
                if (!summary.containsKey(e.name)) {
                    summary.put(e.name, e);
                } else {
                    info = summary.get(e.name);
                    info.errorTime += e.errorTime;
                    info.errors += e.errors;
                    info.hits += e.hits;
                    info.time += e.time;
                }
                localSum.errorTime += e.errorTime;
                localSum.errors += e.errors;
                localSum.hits += e.hits;
                localSum.time += e.time;
            }
            
            // Store the PID summary
            pidName = (String)router.queryTools("pid_name", new Object[] { pid });
            if (pidName == null) pidName = "(Unknown)";
            myPresenter.gridPIDList.addRow(new Object[] {
                pid, pidName, localSum.hits, localSum.errors, localSum.time * 1000, localSum.time * 1000 / localSum.hits
            });
        }

        // Display the summary
        for (CallInfo c: summary.values()) {
            myPresenter.gridGeneralList.addRow(new Object[] { 
                c.name, c.hits, c.errors, c.time * 1000, c.errorTime * 1000, c.time * 1000 / c.hits
            });
        }
    }

    private void showRowDetails(Long forPID) {
        Hashtable<String, CallInfo> entry;

        Integer i = 0;
        for (Long pid: callMap.keySet()) {
            if (pid.equals(forPID)) {
                entry = callMap.get(pid);
                myPresenter.gridPIDDetails.reset();
                for (CallInfo c: entry.values()) {
                    myPresenter.gridPIDDetails.addRow(new Object[] {
                        c.name, c.hits, c.errors, c.time * 1000, c.errorTime * 1000, c.time * 1000 / c.hits
                    });
                }
            }
        }
    }

    @Override
    public void reset() {
        myPresenter.reset();
        callMap.clear();
    }

}

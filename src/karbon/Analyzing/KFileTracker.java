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
import KAnalyzer.Utils.GridPresenterEx;
import java.util.Hashtable;

/**
 *
 * @author Ioannis Charalampidis
 */
public class KFileTracker extends TAnalyzeTarget {

    GridPresenterEx presenter;

    private class FileInfo {
        public String filename = "";
        public Integer accesses = 0;
        public Long calls = 0l;
        public Long bytesRead = 0l;
        public Long bytesWritten = 0l;
        public Double timeSpentWriting = 0d;
        public Double timeSpentReading = 0d;
        public Double timeSpentSeeking = 0d;
        public Double timeTotal = 0d;
        public Double worstTime = 0d;
        public String worstCall = "";
    }

    public KFileTracker() {
        presenter = new GridPresenterEx();
        presenter.Title = "Files I/O";
        presenter.setColumnInfo(new Object[][] {
            { "Filename", String.class, 500 },
            { "Accesses", Long.class, 100 },
            { "Calls", Long.class, 100 },
            { "Written bytes", Long.class, 100 },
            { "Read bytes", Long.class, 100 },
            { "Writing time", Double.class, 100 },
            { "Reading time", Double.class, 100 },
            { "Seeking time", Double.class, 100 },
            { "Total time", Double.class, 100 },
            { "Worst time", Double.class, 100 },
            { "Worst call", String.class, 100 }
        });
    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {
        Hashtable<Long, FDTracker> fileFD = (Hashtable<Long, FDTracker>)router.queryTools("fd_file", null);
        Hashtable<String, FileInfo> map = new Hashtable<String, FileInfo>();
        FileInfo fInf;
        Double fTime;
        for (Long pid: fileFD.keySet()) {
            for (DynamicDescriptor dd: fileFD.get(pid).archive) {
                if (map.containsKey(dd.index)) {
                    fInf = map.get(dd.index);
                } else {
                    fInf = new FileInfo();
                    fInf.filename = dd.index;
                    map.put(dd.index, fInf);
                }

                fInf.accesses++;
                fInf.calls += (Integer)dd.get("calls", 0);
                fInf.bytesRead += (Long)dd.get("bytes_in", 0l);
                fInf.bytesWritten += (Long)dd.get("bytes_out", 0l);
                fInf.timeSpentReading += (Double)dd.get("time_in", 0d);
                fInf.timeSpentWriting += (Double)dd.get("time_out", 0d);
                fInf.timeSpentSeeking += (Double)dd.get("time_seek", 0d);
                fInf.timeTotal += (Double)dd.get("time", 0d);
                fTime = (Double)dd.get("worst_time", 0d);
                if (fTime > fInf.worstTime) {
                    fInf.worstTime = fTime;
                    fInf.worstCall = (String)dd.get("worst_call", "");
                }
            }
        }

        // Display the summary
        FileInfo summary = new FileInfo();
        summary.filename = "<TOTAL>";
        for (FileInfo info: map.values()) {
            presenter.addRow(new Object[] {
                info.filename,
                info.accesses, info.calls,
                info.bytesWritten, info.bytesRead,
                info.timeSpentWriting * 1000,
                info.timeSpentReading * 1000,
                info.timeSpentSeeking * 1000,
                info.timeTotal * 1000,
                info.worstTime * 1000,
                info.worstCall
            });

            summary.accesses += info.accesses;
            summary.calls += info.calls;
            summary.bytesRead += info.bytesRead;
            summary.bytesWritten += info.bytesWritten;
            summary.timeSpentReading += info.timeSpentReading;
            summary.timeSpentSeeking += info.timeSpentSeeking;
            summary.timeSpentWriting += info.timeSpentWriting;
            summary.timeTotal += info.timeTotal;
            if (info.worstTime > summary.worstTime) {
                summary.worstTime = info.worstTime;
                summary.worstCall = info.worstCall;
            }
        }
        
        presenter.addRow(new Object[] {
            summary.filename,
            summary.accesses, summary.calls,
            summary.bytesWritten, summary.bytesRead,
            summary.timeSpentWriting * 1000,
            summary.timeSpentReading * 1000,
            summary.timeSpentSeeking * 1000,
            summary.timeTotal * 1000,
            summary.worstTime * 1000,
            summary.worstCall
        });

    }

    @Override
    public void reset() {
        presenter.reset();
    }

    @Override
    public TPresenter getPresenter() {
        return presenter;
    }

}

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class KTimeUtilization extends TAnalyzeTarget {

    private Object fileRegex[][] = new Object[][] {
        { "Python files", Pattern.compile("^/.*?/Python.*/",Pattern.CANON_EQ) },
        { "ROOT files", Pattern.compile("^/.*?/ROOT.*/",Pattern.CANON_EQ) },
        { "PROOF settings", Pattern.compile("^/.*?/\\.proof/",Pattern.CANON_EQ) },
        { "Temporary files", Pattern.compile("^/tmp/.*",Pattern.CANON_EQ) },
        { "ROOT Source files", Pattern.compile(".*\\.root$",Pattern.CANON_EQ) },
    };

    PTimeUtilization presenter;

    public KTimeUtilization() {
        presenter = new PTimeUtilization();
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
        Double fTime = 0d;
        String fName;
        Double pileTime = 0d;
        Pattern pat;
        Matcher match;
        Boolean found;
        for (Long pid: fileFD.keySet()) {
            for (DynamicDescriptor dd: fileFD.get(pid).archive) {
                fName = (String)dd.index;
                fTime = (Double)dd.get("time", 0d);

                found = false;
                for (Object[] o: fileRegex) {
                    pat = (Pattern) o[1];
                    match = pat.matcher(fName);
                    if (match.matches()) {
                        found = true;
                        presenter.pileTimeDetail("File I/O", (String)o[0], fTime);
                        break;
                    }
                }
                if (!found) {
                    presenter.pileTimeDetail("File I/O", "Misc file I/O", fTime);
                }
                
                presenter.pileTime("File I/O", fTime);
                pileTime += fTime;
            }
        }

        fileFD = (Hashtable<Long, FDTracker>)router.queryTools("fd_socket", null);
        String fLocal, fRemote, fFamily, fType;
        for (Long pid: fileFD.keySet()) {
            for (DynamicDescriptor dd: fileFD.get(pid).archive) {
                fLocal = (String)dd.get("local","");
                fRemote = (String)dd.get("remote","");
                fTime = (Double)dd.get("time", 0d);
                fFamily = (String)dd.get("family", "");
                fType = (String)dd.get("type", "");

                if (fFamily.equals("PF_INET")) {
                    if (fType.equals("SOCK_DGRAM")) {
                        presenter.pileTimeDetail("Network I/O", "UDP Sockets", fTime);
                    } else if (fType.equals("SOCK_STREAM")) {
                        presenter.pileTimeDetail("Network I/O", "TCP Sockets", fTime);
                    } else {
                        presenter.pileTimeDetail("Network I/O", "Unknown INET Sockets", fTime);
                    }
                } else if (fFamily.equals("PF_FILE")) {
                    presenter.pileTimeDetail("Network I/O", "UNIX Sockets", fTime);
                }

                presenter.pileTime("Network I/O", fTime);
                pileTime += fTime;
            }
        }

        pileTime = (Double)router.queryTools("call_time", new Object[] { }) - pileTime;
        presenter.pileTime("Misc time", pileTime);

        presenter.display(
            (Double)router.queryTools("total_time", new Object[] { }),
            (Double)router.queryTools("call_time", new Object[] { })
        );

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

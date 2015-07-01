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
 * @author Γιάννης
 */
public class KNetworkTracker extends TAnalyzeTarget {

    PNetworkPresenter presenter;

    private enum SocketClass {
        TCP, UDP, UNIX, Unknown
    }

    private class SocketInfo {
        public SocketClass socketClass = SocketClass.Unknown;
        public String index = "";
        public String local = "";
        public String remote = "";
        public Integer accesses = 0;
        public Long calls = 0l;
        public Long bytesRead = 0l;
        public Long bytesWritten = 0l;
        public Double timeSpentSending = 0d;
        public Double timeSpentReceiving = 0d;
        public Double timeSpentWaiting = 0d;
        public Double timeSpentConnecting = 0d;
        public Double timeTotal = 0d;
        public Double worstTime = 0d;
        public String worstCall = "";
    }

    public KNetworkTracker() {
        presenter = new PNetworkPresenter();
    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {
        Hashtable<Long, FDTracker> fileFD = (Hashtable<Long, FDTracker>)router.queryTools("fd_socket", null);
        Hashtable<String, SocketInfo> map = new Hashtable<String, SocketInfo>();
        SocketInfo fInf;
        Double fTime;
        String index, sLocal, sRemote, pFamily, pType;
        for (Long pid: fileFD.keySet()) {
            for (DynamicDescriptor dd: fileFD.get(pid).archive) {
                sLocal = (String)dd.get("local","");
                if ("".equals(sLocal)) sLocal = "???";
                sRemote = (String)dd.get("remote", "");
                if ("".equals(sRemote)) sRemote = "???";
                index = sLocal + " -> " + sRemote;

                if (map.containsKey(index)) {
                    fInf = map.get(index);
                } else {
                    fInf = new SocketInfo();
                    fInf.index = index;
                    fInf.local = sLocal;
                    fInf.remote = sRemote;
                    map.put(index, fInf);
                }

                fInf.accesses++;
                fInf.calls += (Integer)dd.get("calls", 0);
                fInf.bytesRead += (Long)dd.get("bytes_in", 0l);
                fInf.bytesWritten += (Long)dd.get("bytes_out", 0l);
                fInf.timeSpentReceiving += (Double)dd.get("time_in", 0d);
                fInf.timeSpentSending += (Double)dd.get("time_out", 0d);
                fInf.timeSpentWaiting += (Double)dd.get("time_wait", 0d);
                fInf.timeSpentConnecting += (Double)dd.get("time_connect", 0d);
                fInf.timeTotal += (Double)dd.get("time", 0d);
                fTime = (Double)dd.get("worst_time", 0d);
                if (fTime > fInf.worstTime) {
                    fInf.worstTime = fTime;
                    fInf.worstCall = (String)dd.get("worst_call", "");
                }

                pFamily = (String)dd.get("family", "AF_UNKNOWN");
                pType = (String)dd.get("type", "AF_UNKNOWN");
                if (pFamily.equals("PF_INET")) {
                    if (pType.equals("SOCK_DGRAM")) {
                        fInf.socketClass = SocketClass.UDP;
                    } else if (pType.equals("SOCK_STREAM")) {
                        fInf.socketClass = SocketClass.TCP;
                    }
                } else if (pFamily.equals("PF_FILE")) {
                    fInf.socketClass = SocketClass.UNIX;
                }
            }
        }

        // Display the summary
        SocketInfo summaries[] = new SocketInfo[4];
        summaries[0] = new SocketInfo(); // General
        summaries[0].index = "<TOTAL>";
        summaries[1] = new SocketInfo(); // TCP
        summaries[1].index = "<TOTAL>";
        summaries[2] = new SocketInfo(); // UDP
        summaries[2].index = "<TOTAL>";
        summaries[3] = new SocketInfo(); // UNIX
        summaries[3].index = "<TOTAL>";

        for (SocketInfo info: map.values()) {

            // TCP Summary
            if (info.socketClass == SocketClass.TCP) {
                presenter.gridTCP.addRow(new Object[] {
                    info.local,
                    info.remote,
                    info.accesses, info.calls,
                    info.bytesWritten, info.bytesRead,
                    info.timeSpentSending * 1000,
                    info.timeSpentReceiving * 1000,
                    info.timeSpentWaiting * 1000,
                    info.timeSpentConnecting * 1000,
                    info.timeTotal * 1000,
                    info.worstTime * 1000,
                    info.worstCall
                });

                summaries[1].accesses += info.accesses;
                summaries[1].calls += info.calls;
                summaries[1].bytesRead += info.bytesRead;
                summaries[1].bytesWritten += info.bytesWritten;
                summaries[1].timeSpentConnecting += info.timeSpentConnecting;
                summaries[1].timeSpentReceiving += info.timeSpentReceiving;
                summaries[1].timeSpentSending += info.timeSpentSending;
                summaries[1].timeSpentWaiting += info.timeSpentWaiting;
                summaries[1].timeTotal += info.timeTotal;
                if (info.worstTime > summaries[1].worstTime) {
                    summaries[1].worstTime = info.worstTime;
                    summaries[1].worstCall = info.worstCall;
                }

            // UDP Summary
            } else if (info.socketClass == SocketClass.UDP) {
                presenter.gridUDP.addRow(new Object[] {
                    info.local,
                    info.remote,
                    info.accesses, info.calls,
                    info.bytesWritten, info.bytesRead,
                    info.timeSpentSending * 1000,
                    info.timeSpentReceiving * 1000,
                    info.timeSpentWaiting * 1000,
                    info.timeSpentConnecting * 1000,
                    info.timeTotal * 1000,
                    info.worstTime * 1000,
                    info.worstCall
                });

                summaries[2].accesses += info.accesses;
                summaries[2].calls += info.calls;
                summaries[2].bytesRead += info.bytesRead;
                summaries[2].bytesWritten += info.bytesWritten;
                summaries[2].timeSpentConnecting += info.timeSpentConnecting;
                summaries[2].timeSpentReceiving += info.timeSpentReceiving;
                summaries[2].timeSpentSending += info.timeSpentSending;
                summaries[2].timeSpentWaiting += info.timeSpentWaiting;
                summaries[2].timeTotal += info.timeTotal;
                if (info.worstTime > summaries[2].worstTime) {
                    summaries[2].worstTime = info.worstTime;
                    summaries[2].worstCall = info.worstCall;
                }

            // UNIX Summary
            } else if (info.socketClass == SocketClass.UNIX) {
                presenter.gridUNIX.addRow(new Object[] {
                    info.local,
                    info.remote,
                    info.accesses, info.calls,
                    info.bytesWritten, info.bytesRead,
                    info.timeSpentSending * 1000,
                    info.timeSpentReceiving * 1000,
                    info.timeSpentWaiting * 1000,
                    info.timeSpentConnecting * 1000,
                    info.timeTotal * 1000,
                    info.worstTime * 1000,
                    info.worstCall
                });

                summaries[3].accesses += info.accesses;
                summaries[3].calls += info.calls;
                summaries[3].bytesRead += info.bytesRead;
                summaries[3].bytesWritten += info.bytesWritten;
                summaries[3].timeSpentConnecting += info.timeSpentConnecting;
                summaries[3].timeSpentReceiving += info.timeSpentReceiving;
                summaries[3].timeSpentSending += info.timeSpentSending;
                summaries[3].timeSpentWaiting += info.timeSpentWaiting;
                summaries[3].timeTotal += info.timeTotal;
                if (info.worstTime > summaries[3].worstTime) {
                    summaries[3].worstTime = info.worstTime;
                    summaries[3].worstCall = info.worstCall;
                }

            }

            summaries[0].accesses += info.accesses;
            summaries[0].calls += info.calls;
            summaries[0].bytesRead += info.bytesRead;
            summaries[0].bytesWritten += info.bytesWritten;
            summaries[0].timeSpentConnecting += info.timeSpentConnecting;
            summaries[0].timeSpentReceiving += info.timeSpentReceiving;
            summaries[0].timeSpentSending += info.timeSpentSending;
            summaries[0].timeSpentWaiting += info.timeSpentWaiting;
            summaries[0].timeTotal += info.timeTotal;
            if (info.worstTime > summaries[0].worstTime) {
                summaries[0].worstTime = info.worstTime;
                summaries[0].worstCall = info.worstCall;
            }

        }

       // Total Rows
       presenter.gridOverall.addRow(new Object[] {
            summaries[0].index,
            summaries[0].accesses, summaries[0].calls,
            summaries[0].bytesWritten, summaries[0].bytesRead,
            summaries[0].timeSpentSending * 1000,
            summaries[0].timeSpentReceiving * 1000,
            summaries[0].timeSpentWaiting * 1000,
            summaries[0].timeSpentConnecting * 1000,
            summaries[0].timeTotal * 1000,
            summaries[0].worstTime * 1000,
            summaries[0].worstCall
        });
        presenter.gridTCP.addRow(new Object[] {
            summaries[1].index, "",
            summaries[1].accesses, summaries[1].calls,
            summaries[1].bytesWritten, summaries[1].bytesRead,
            summaries[1].timeSpentSending * 1000,
            summaries[1].timeSpentReceiving * 1000,
            summaries[1].timeSpentWaiting * 1000,
            summaries[1].timeSpentConnecting * 1000,
            summaries[1].timeTotal * 1000,
            summaries[1].worstTime * 1000,
            summaries[1].worstCall
        });
        presenter.gridUDP.addRow(new Object[] {
            summaries[1].index, "",
            summaries[2].accesses, summaries[2].calls,
            summaries[2].bytesWritten, summaries[2].bytesRead,
            summaries[2].timeSpentSending * 1000,
            summaries[2].timeSpentReceiving * 1000,
            summaries[2].timeSpentWaiting * 1000,
            summaries[2].timeSpentConnecting * 1000,
            summaries[2].timeTotal * 1000,
            summaries[2].worstTime * 1000,
            summaries[2].worstCall
        });
        presenter.gridUNIX.addRow(new Object[] {
            summaries[1].index, "",
            summaries[3].accesses, summaries[3].calls,
            summaries[3].bytesWritten, summaries[3].bytesRead,
            summaries[3].timeSpentSending * 1000,
            summaries[3].timeSpentReceiving * 1000,
            summaries[3].timeSpentWaiting * 1000,
            summaries[3].timeSpentConnecting * 1000,
            summaries[3].timeTotal * 1000,
            summaries[3].worstTime * 1000,
            summaries[3].worstCall
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

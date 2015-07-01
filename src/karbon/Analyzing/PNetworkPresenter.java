/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PNetworkPresenter.java
 *
 * Created on 29 Αυγ 2010, 10:02:17 μμ
 */

package karbon.Analyzing;

import KAnalyzer.API.TPresenter;
import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * @author Γιάννης
 */
public class PNetworkPresenter extends TPresenter {

    /** Creates new form PNetworkPresenter */
    public PNetworkPresenter() {
        initComponents();
        Title = "Network I/O";
        
        gridOverall.Title = "Overall Network I/O Statistics";
        gridOverall.setColumnInfo(new Object[][] {
            { "Connection", String.class, 500 },
            { "Accesses", Long.class, 100 },
            { "Calls", Long.class, 100 },
            { "Sent bytes", Long.class, 100 },
            { "Received bytes", Long.class, 100 },
            { "Sending time", Double.class, 100 },
            { "Receiving time", Double.class, 100 },
            { "Waiting time", Double.class, 100 },
            { "Connecting time", Double.class, 100 },
            { "Total time", Double.class, 100 },
            { "Worst time", Double.class, 100 },
            { "Worst call", String.class, 100 }
        });

        gridTCP.Title = "TCP Statistics";
        gridTCP.setColumnInfo(new Object[][] {
            { "Bind", String.class, 250 },
            { "Connect", String.class, 250 },
            { "Accesses", Long.class, 100 },
            { "Calls", Long.class, 100 },
            { "Sent bytes", Long.class, 100 },
            { "Received bytes", Long.class, 100 },
            { "Sending time", Double.class, 100 },
            { "Receiving time", Double.class, 100 },
            { "Waiting time", Double.class, 100 },
            { "Connecting time", Double.class, 100 },
            { "Total time", Double.class, 100 },
            { "Worst time", Double.class, 100 },
            { "Worst call", String.class, 100 }
        });

        gridUDP.Title = "UDP Statistics";
        gridUDP.setColumnInfo(new Object[][] {
            { "Bind", String.class, 250 },
            { "Connect", String.class, 250 },
            { "Accesses", Long.class, 100 },
            { "Calls", Long.class, 100 },
            { "Sent bytes", Long.class, 100 },
            { "Received bytes", Long.class, 100 },
            { "Sending time", Double.class, 100 },
            { "Receiving time", Double.class, 100 },
            { "Waiting time", Double.class, 100 },
            { "Connecting time", Double.class, 100 },
            { "Total time", Double.class, 100 },
            { "Worst time", Double.class, 100 },
            { "Worst call", String.class, 100 }
        });

        gridUNIX.Title = "UNIX Sockets Statistics";
        gridUNIX.setColumnInfo(new Object[][] {
            { "Bind", String.class, 250 },
            { "Connect", String.class, 250 },
            { "Accesses", Long.class, 100 },
            { "Calls", Long.class, 100 },
            { "Sent bytes", Long.class, 100 },
            { "Received bytes", Long.class, 100 },
            { "Sending time", Double.class, 100 },
            { "Receiving time", Double.class, 100 },
            { "Waiting time", Double.class, 100 },
            { "Connecting time", Double.class, 100 },
            { "Total time", Double.class, 100 },
            { "Worst time", Double.class, 100 },
            { "Worst call", String.class, 100 }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        gridOverall = new KAnalyzer.Utils.GridPresenterEx();
        gridUNIX = new KAnalyzer.Utils.GridPresenterEx();
        gridTCP = new KAnalyzer.Utils.GridPresenterEx();
        gridUDP = new KAnalyzer.Utils.GridPresenterEx();

        jTabbedPane1.addTab("Overall", gridOverall);
        jTabbedPane1.addTab("UNIX Sockets", gridUNIX);
        jTabbedPane1.addTab("TCP Sockets", gridTCP);
        jTabbedPane1.addTab("UDP Sockets", gridUDP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public KAnalyzer.Utils.GridPresenterEx gridOverall;
    public KAnalyzer.Utils.GridPresenterEx gridTCP;
    public KAnalyzer.Utils.GridPresenterEx gridUDP;
    public KAnalyzer.Utils.GridPresenterEx gridUNIX;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void reset() {
        gridOverall.reset();
        gridTCP.reset();
        gridUDP.reset();
        gridUNIX.reset();
    }

    @Override
    public String getReport(ReportFormat format) {
        String report = "";
        if (gridOverall.includeToReport()) report += gridOverall.getReport(format);
        if (gridTCP.includeToReport()) report += gridTCP.getReport(format);
        if (gridUDP.includeToReport()) report += gridUDP.getReport(format);
        if (gridUNIX.includeToReport()) report += gridUNIX.getReport(format);
        return report;
    }

    @Override
    public boolean includeToReport() {
        if (gridOverall.includeToReport()) return true;
        if (gridTCP.includeToReport()) return true;
        if (gridUDP.includeToReport()) return true;
        if (gridUNIX.includeToReport()) return true;
        return false;
    }

}
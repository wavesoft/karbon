/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PHotspotPresenter.java
 *
 * Created on 28 Αυγ 2010, 11:54:55 μμ
 */

package karbon.Analyzing;

import KAnalyzer.API.TPresenter;
import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * @author Ioannis Charalampidis
 */
public class PHotspotPresenter extends TPresenter {

    /** Creates new form PHotspotPresenter */
    public PHotspotPresenter() {
        initComponents();

        Title = "Hot Spots";

        gridCalls.Title = "System call hotspots";
        gridCalls.setColumnInfo(new Object[][] {
            { "Call", String.class, 500 },
            { "Success %", Double.class, 100 },
            { "Failure %", Double.class, 100 },
            { "Total %", Double.class, 100 }
        });

        gridFilesystem.Title = "Filesystem hotspots";
        gridFilesystem.setColumnInfo(new Object[][] {
            { "Filename", String.class, 500 },
            { "Success %", Double.class, 100 },
            { "Failure %", Double.class, 100 },
            { "Total %", Double.class, 100 }
        });

        gridProccess.Title = "Process hotspots";
        gridProccess.setColumnInfo(new Object[][] {
            { "PID", String.class, 100 },
            { "Image name", String.class, 500 },
            { "Arguments", String.class, 500 },
            { "Success %", Double.class, 100 },
            { "Failure %", Double.class, 100 },
            { "Total %", Double.class, 100 }
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
        gridCalls = new KAnalyzer.Utils.GridPresenterEx();
        gridFilesystem = new KAnalyzer.Utils.GridPresenterEx();
        gridProccess = new KAnalyzer.Utils.GridPresenterEx();

        jTabbedPane1.addTab("System call hotspots", gridCalls);
        jTabbedPane1.addTab("Filesystem hotspots", gridFilesystem);
        jTabbedPane1.addTab("Process hotspots", gridProccess);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public KAnalyzer.Utils.GridPresenterEx gridCalls;
    public KAnalyzer.Utils.GridPresenterEx gridFilesystem;
    public KAnalyzer.Utils.GridPresenterEx gridProccess;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void reset() {
        gridCalls.reset();
        gridFilesystem.reset();
        gridProccess.reset();
    }

    @Override
    public String getReport(ReportFormat format) {
        String buffer = "";
        if (gridCalls.includeToReport()) buffer += gridCalls.getReport(format);
        if (gridFilesystem.includeToReport()) buffer += gridFilesystem.getReport(format);
        if (gridProccess.includeToReport()) buffer += gridProccess.getReport(format);
        return buffer;
    }

    @Override
    public boolean includeToReport() {
        if (gridCalls.includeToReport()) return true;
        if (gridFilesystem.includeToReport()) return true;
        if (gridProccess.includeToReport()) return true;
        return false;
    }

}

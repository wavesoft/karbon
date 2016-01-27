/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PGeneralPresenter.java
 *
 * Created on 23 Αυγ 2010, 4:36:53 μμ
 */

package karbon.Analyzing;

import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * @author Ioannis Charalampidis
 */
public class PCallsPresenter extends KAnalyzer.API.TPresenter  {

    /** Creates new form PGeneralPresenter */
    public PCallsPresenter() {
        initComponents();
        gridPIDList.Title = "System calls per PID";
        gridPIDDetails.Title = "Details of selected PID";
        gridGeneralList.Title = "System calls";
        reset();
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
        gridGeneralList = new KAnalyzer.Utils.GridPresenterEx();
        jSplitPane1 = new javax.swing.JSplitPane();
        gridPIDList = new KAnalyzer.Utils.GridPresenterEx();
        gridPIDDetails = new KAnalyzer.Utils.GridPresenterEx();

        jTabbedPane1.addTab("General", gridGeneralList);

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setTopComponent(gridPIDList);
        jSplitPane1.setRightComponent(gridPIDDetails);

        jTabbedPane1.addTab("Details", jSplitPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void reset() {
        gridPIDList.reset();
        gridPIDDetails.reset();
        gridPIDList.reset();
        gridPIDList.target = this.target;
        gridPIDDetails.target = this.target;
        gridPIDList.target = this.target;
    }

    @Override
    public String getReport(ReportFormat format) {
        String buffer = "";
        if (gridGeneralList.includeToReport()) buffer+=gridGeneralList.getReport(format);
        if (gridPIDDetails.includeToReport()) buffer+=gridPIDDetails.getReport(format);
        if (gridPIDList.includeToReport()) buffer+=gridPIDList.getReport(format);
        return buffer;
    }

    @Override
    public boolean includeToReport() {
        return gridGeneralList.includeToReport() || gridPIDDetails.includeToReport() || gridPIDList.includeToReport();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public KAnalyzer.Utils.GridPresenterEx gridGeneralList;
    public KAnalyzer.Utils.GridPresenterEx gridPIDDetails;
    public KAnalyzer.Utils.GridPresenterEx gridPIDList;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}

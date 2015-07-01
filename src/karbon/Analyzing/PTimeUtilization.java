/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PPileTime.java
 *
 * Created on 31 Αυγ 2010, 10:54:59 πμ
 */

package karbon.Analyzing;

import KAnalyzer.API.TPresenter;
import KAnalyzer.Utils.ReportTools.ReportFormat;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * Time classification presenter for use with KPileTime
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class PTimeUtilization extends TPresenter {

    public Hashtable<String, Double> times;
    public Hashtable<String, Hashtable<String, Double>> timeDetails;

    private static final Color[] colorTable = new Color[] {
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA,
        Color.CYAN, Color.ORANGE, Color.LIGHT_GRAY,
        Color.PINK
    };

    private Double totalProcTime;
    private Double totalCallTime;

    /** Creates new form PPileTime */
    public PTimeUtilization() {
        initComponents();
        times = new Hashtable<String, Double>();
        Title = "Time Utilization";
        gridTimes.Title = "Time Utilization";
        gridTimes.setColumnInfo(new Object[][]{
            { "Category", String.class, 500 },
            { "Time (ms)", Double.class, 100 },
            { "Normalized time (ms)", Double.class, 100 },
        });        
        timeDetails = new Hashtable<String, Hashtable<String, Double>>();
        gridDetails.Title = "Detailed time Utilization";
        gridDetails.setColumnInfo(new Object[][]{
            { "Category", String.class, 500 },
            { "Time (ms)", Double.class, 100 },
            { "Normalized time (ms)", Double.class, 100 },
        });

        gridTimes.jGridData.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                int index = gridTimes.jGridData.getSelectedRow();
                String entry = (String) gridTimes.jGridData.getModel().getValueAt(index, 0);
                gridDetails.reset();
                if (!timeDetails.containsKey(entry)) return;

                Hashtable<String, Double> timeInfo = timeDetails.get(entry);
                
                gridDetails.Title = "Detailed time Utilization of " + entry;
                Double partialCallTime = (Double) gridTimes.jGridData.getModel().getValueAt(index, 1) / 1000;
                Double partialProcTime = (Double) gridTimes.jGridData.getModel().getValueAt(index, 2) / 1000;

                Double oldTime = 0d, time = 0d, totalTime = 0d, stackTotalTime = 0d, procTime = 0d;
                
                for (String k: timeInfo.keySet()) {
                    time = timeInfo.get(k);
                    procTime = totalProcTime * time / totalCallTime;
                    gridDetails.addRow(new Object[] {
                        k, time * 1000, procTime * 1000
                    });
                    totalTime += time;
                    stackTotalTime += procTime;
                }
                gridDetails.addRow(new Object[] {
                    "<Total>", totalTime * 1000, stackTotalTime * 1000
                });
                //gridDetails.addRow(new Object[] {
                //    "<Expected>", partialCallTime * 1000, partialProcTime * 1000
                //});

            }
        });
    }

    public void pileTime(String category, Double time) {
        Double oldTime = 0d;
        if (times.containsKey(category)) {
            oldTime = times.get(category);
        }
        oldTime += time;
        times.put(category, oldTime);
    }

    public void pileTimeDetail(String category, String subCategory, Double time) {
        Hashtable<String, Double> hashTable;
        if (timeDetails.containsKey(category)) {
            hashTable = timeDetails.get(category);
        } else {
            hashTable = new Hashtable<String, Double>();
            timeDetails.put(category, hashTable);
        }

        Double oldTime = 0d;
        if (hashTable.containsKey(subCategory)) {
            oldTime = hashTable.get(subCategory);
        }
        oldTime += time;
        hashTable.put(subCategory, oldTime);
    }

    public void display(Double totalProcTime, Double totalCallTime) {
        Double time, procTime;
        Double totalTime = 0d, sumProcTime = 0d;
        for (String k: times.keySet()) {
            time = times.get(k);
            procTime = totalProcTime * time / totalCallTime;
            gridTimes.addRow(new Object[] {
                k, time * 1000, procTime * 1000
            });
            totalTime += time;
            sumProcTime += procTime;
        }
        gridTimes.addRow(new Object[] {
            "<Total>", totalTime * 1000, sumProcTime * 1000
        });
        //gridTimes.addRow(new Object[] {
        //    "<Expected>", totalCallTime * 1000, totalProcTime * 1000
        //});

        this.totalCallTime = totalCallTime;
        this.totalProcTime = totalProcTime;
        jTextField1.setText(String.format("%,4f", totalProcTime));
    }

    private void repantPanel(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jGraph = new javax.swing.JPanel(){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                repantPanel(g);
            }
        };
        jSplitPane1 = new javax.swing.JSplitPane();
        gridTimes = new KAnalyzer.Utils.GridPresenterEx();
        gridDetails = new KAnalyzer.Utils.GridPresenterEx();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        jGraph.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jGraphLayout = new javax.swing.GroupLayout(jGraph);
        jGraph.setLayout(jGraphLayout);
        jGraphLayout.setHorizontalGroup(
            jGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 357, Short.MAX_VALUE)
        );
        jGraphLayout.setVerticalGroup(
            jGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 68, Short.MAX_VALUE)
        );

        jSplitPane1.setDividerLocation(120);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setTopComponent(gridTimes);
        jSplitPane1.setRightComponent(gridDetails);

        jLabel1.setText("Normalize to maximum value:");

        jTextField1.setText("0.00");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setText("Update");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jGraph, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            Double max = Double.parseDouble(jTextField1.getText());
            gridDetails.reset();
            gridTimes.reset();
            display( max , totalCallTime);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: "+e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private KAnalyzer.Utils.GridPresenterEx gridDetails;
    private KAnalyzer.Utils.GridPresenterEx gridTimes;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jGraph;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void reset() {
        times.clear();
        timeDetails.clear();
        gridTimes.reset();
        gridDetails.reset();
    }

    @Override
    public String getReport(ReportFormat format) {
        String buffer = "";
        if (gridTimes.includeToReport()) buffer += gridTimes.getReport(format);
        if (gridDetails.includeToReport()) buffer += gridDetails.getReport(format);
        return buffer;
    }

    @Override
    public boolean includeToReport() {
        if (gridTimes.includeToReport()) return true;
        if (gridDetails.includeToReport()) return true;
        return false;
    }

}

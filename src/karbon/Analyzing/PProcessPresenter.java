/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PProcessPresenter.java
 *
 * Created on 22 Αυγ 2010, 12:30:21 πμ
 */

package karbon.Analyzing;

import KAnalyzer.Utils.GridPresenterEx;
import KAnalyzer.Utils.ReportTools;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.tree.TreeModel;
import KAnalyzer.Utils.ReportTools.ReportFormat;
import java.awt.FontMetrics;
import java.awt.Point;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class PProcessPresenter extends KAnalyzer.API.TPresenter {

    public ArrayList<KProcessTracker.ProcessInfo> procInfo;
    public Double procMaxTime = 0d;
    public Double procMinTime = 0d;

    private GridPresenterEx pidGrid;
    private Integer mouseX = 0;
    private Integer mouseY = 0;
    private Integer stickerX = 0;

    /** Creates new form PProcessPresenter */
    public PProcessPresenter() {
        initComponents();

        pidGrid = new GridPresenterEx();
        jTabbedPane1.add("Resolved PIDs" ,pidGrid);
        pidGrid.Title = "Resolved PIDs";
        pidGrid.setColumnInfo(new Object[][] {
            {"PID", Integer.class, 100},
            {"Image", String.class, 600},
            {"Arguments", String.class, 600}
        });
        
    }

    public void RedrawGraph() {

        //jGraphPanel.removeAll();
        if (procInfo == null) return;

        JLabel panel;

        Integer height, maxWidth = 0;

        height = 35;
        maxWidth = jScrollPane3.getWidth() * jSlider1.getValue() / 1000;
        
        jGraphPanel.setPreferredSize(new Dimension(maxWidth + 10 ,height*procInfo.size()));
        jGraphPanel.revalidate();
        jGraphPanel.repaint();

        /*
        Integer yHeight = 20;
        Integer top = 0;
        Integer width = jGraphPanel.getWidth();
        Double left = 0D, right = 0D;
        Integer MaxWidth = 0;

        procMaxTime = 0.001;

        for (KProcessTracker.ProcessInfo proc : procInfo) {
            panel = new JLabel();
            jGraphPanel.add(panel);

            panel.setVisible(true);
            left = width.DoubleValue() * proc.started / procMaxTime;
            right = width.DoubleValue() * proc.stopped / procMaxTime;
            if (proc.stopped == procMaxTime) {
                right = Double.valueOf(width);
            }
            panel.setLocation(left.intValue(), top);
            panel.setSize(right.intValue() - left.intValue(), yHeight-2);
            if (right > MaxWidth) {
                MaxWidth = right.intValue();
            }
            top += yHeight;

            panel.setOpaque(true);
            panel.setBackground(Color.red);
            panel.setText(proc.imageName);
            panel.setToolTipText(proc.imageName + " (PID: "+proc.pid.toString()+", Parent PID: "+proc.parent.toString()+")");
        }
         */

    }
    
    public void repantPanel(Graphics g) {
        //jGraphPanel.removeAll();
        if (procInfo == null) return;
        Graphics2D g2d = (Graphics2D) g;

        Integer left, top, right, width, height, maxWidth = 0;
        Double dLeft, dRight;

        /*** Calculate scales ***/
        Double xScale = (jScrollPane3.getWidth() * jSlider1.getValue() / 1000) / (procMaxTime  - procMinTime);
        maxWidth = jScrollPane3.getWidth() * jSlider1.getValue() / 1000;

        /*** Design the actual range ***/
        //g.setColor(Color.darkGray);
        //g.fillRect(0, 0, jGraphPanel.getWidth(), jGraphPanel.getHeight());
        g.setColor(Color.decode("0x333333"));
        g.fillRect(0, 0, ((Double)((procMaxTime - procMinTime) * xScale)).intValue(), jGraphPanel.getHeight());

        /*** Design 10ms lines ***/
        /*
        Integer x,y;
        Integer gridSize = xScale.intValue();
        if (gridSize < 1) gridSize = 1;

        Double dash1[] = {1.0f};
        BasicStroke dashed = new BasicStroke(1.0f,
                          BasicStroke.CAP_BUTT,
                          BasicStroke.JOIN_MITER,
                          10.0f, dash1, 0.0f);

        g2d.setStroke(dashed);
        g2d.setColor(Color.gray);
        for (x = gridSize; x < maxWidth; x+= gridSize) {
            g2d.drawLine(x, 0, x, jGraphPanel.getHeight());
        }
        for (y = gridSize; y < jGraphPanel.getHeight(); y+= gridSize) {
            g2d.drawLine(0, y, jGraphPanel.getWidth(),y);
        }
        g2d.setStroke(new BasicStroke(1));
         */


        /*** Render the bargraph ***/
        FontMetrics fm = jGraphPanel.getFontMetrics(jGraphPanel.getFont());
        height = 20;
        top = 15;
        Double timeSpan  = 0.0;
        String text = "";
        for (KProcessTracker.ProcessInfo proc : procInfo) {

            dLeft = xScale * (proc.started - procMinTime);
            dRight = xScale * (proc.stopped - procMinTime);

            left = dLeft.intValue();
            right = dRight.intValue();
            if (right.equals(left)) right = left+2;
            width = right - left;
            if (right + 5 > maxWidth) maxWidth = right + 5;

            g.setColor(Color.red);
            g.fillRect(left, top, width, height-2);

            g2d.setColor(Color.orange);
            g2d.drawLine(right, top-2, right, top + height);
            g2d.setColor(Color.white);
            g2d.drawLine(left, top-15, left, top + height);

            g2d.setColor(Color.lightGray);
            g2d.setFont(jGraphPanel.getFont());
            g2d.drawString( "(" + proc.pid.toString() + ") "+ proc.imageName, left + 2, top - 5);

            timeSpan = (proc.stopped - proc.started) * 1000;
            text = String.format("%,.2f ms", timeSpan);
            g2d.setColor(Color.decode("0xFFBBBB"));
            if (right - fm.stringWidth(text) -2 < left) {
                g2d.drawString( text , right + 2, top + 13);
            } else {
                g2d.drawString( text , right - fm.stringWidth(text) - 2, top + 13);
            }

            top += height + 15;
        }

        // Draw cursor
        g2d.setColor(Color.yellow);
        g2d.drawLine(mouseX, 0, mouseX, jGraphPanel.getHeight());
        timeSpan = ((procMaxTime - procMinTime) * mouseX / maxWidth) * 1000;
        g2d.drawString(String.format("%,.4f ms", timeSpan), mouseX + 4, mouseY);

        // Draw sicky line
        if (stickerX != 0) {
            g2d.setColor(Color.green);
            g2d.drawLine(stickerX, 0, stickerX, jGraphPanel.getHeight());
        }


        //jCursor.repaint();
        //jCursorLabel.repaint();
    }

    public void AddPIDMap(Long PID, String exec, String args) {
        //listModel.addElement(PID.toString() + " = " + exec);
        pidGrid.addRow(new Object[] { PID, exec, args });
    }

    public void setTreeModel(TreeModel m) {
        jTree1.setModel(m);
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
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jGraphPanel = new javax.swing.JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                repantPanel(g);
            }
        }
        ;
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Process Tree", jPanel3);

        jScrollPane3.setBorder(null);
        jScrollPane3.setAutoscrolls(true);
        jScrollPane3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jGraphPanel.setBackground(new java.awt.Color(102, 102, 102));
        jGraphPanel.setFont(new java.awt.Font("Tahoma", 0, 10));
        jGraphPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jGraphPanelMouseClicked(evt);
            }
        });
        jGraphPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jGraphPanelMouseMoved(evt);
            }
        });
        jGraphPanel.setLayout(null);
        jScrollPane3.setViewportView(jGraphPanel);

        jLabel1.setText("Zoom:");

        jSlider1.setMaximum(10000);
        jSlider1.setMinimum(1);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        jSlider1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSlider1PropertyChange(evt);
            }
        });

        jButton1.setText("Save JPG");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("Detailed profiling");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSlider1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggleButton1))))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Process Graph", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSlider1PropertyChange
    }//GEN-LAST:event_jSlider1PropertyChange

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        RedrawGraph();
    }//GEN-LAST:event_jSlider1StateChanged

    private void jGraphPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jGraphPanelMouseMoved
        Point p = evt.getPoint();

        /*
        Double timeSpan = (procMaxTime-procMinTime) * p.x / jGraphPanel.getWidth();
        jCursorLabel.setText(String.format("%,4f ms", timeSpan));

        jCursor.setLocation(p.x, 0);
        jCursor.setSize(1, jGraphPanel.getHeight());
        jCursorLabel.setLocation(p.x+5,p.y-5-jCursorLabel.getHeight());

        jCursorLabel.repaint();
        jCursor.repaint();
         */

        mouseX = p.x;
        mouseY = p.y;
        
        jGraphPanel.repaint();

    }//GEN-LAST:event_jGraphPanelMouseMoved

    private void jGraphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jGraphPanelMouseClicked
        if (evt.getButton() == 1) {
            stickerX = evt.getX();
            jGraphPanel.repaint();
        } else if (evt.getButton() == 3) {
            stickerX = 0;
            jGraphPanel.repaint();
        }
    }//GEN-LAST:event_jGraphPanelMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String filename = ReportTools.saveAsDialog("Save JPEG", new Object[][] { {"JPeg Files", new String[] { "jpg", "jpeg"  } } });
        if (!filename.equals("")) {
            ReportTools.snapshotComponentToJPEG(jGraphPanel, filename);
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jGraphPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getReport(ReportFormat format) {
        String buffer = "";
        if (pidGrid.includeToReport()) buffer+= pidGrid.getReport(format);
        return buffer;
    }

    @Override
    public void reset() {
        pidGrid.reset();        
    }

    @Override
    public boolean includeToReport() {
        return true;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Main.java
 *
 * Created on 26 Ιουλ 2010, 11:29:25 μμ
 */

package karbon;

import KAnalyzer.API.IReportable;
import KAnalyzer.ErrorCollector;
import KAnalyzer.API.TPresenter;
import KAnalyzer.Interfaces.TStream;
import KAnalyzer.Utils.FileStream;
import KAnalyzer.Utils.UDPStream;
import KAnalyzer.Utils.ReportTools;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import KAnalyzer.Utils.ReportTools.ReportFormat;
import KAnalyzer.Utils.TCPStream;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import karbon.PluginDB.PluginInstance;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class GUI extends javax.swing.JFrame {

    SwingWorker worker;

    private class KarbonPluginListRenderer extends JCheckBox implements ListCellRenderer {

        private PluginInstance item;

        public KarbonPluginListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PluginInstance l = (PluginInstance)value;
            this.item = l;
            if (l.type == PluginDB.PlugintType.PLUGIN_ANALYZER) {
                setText("<html><b>"+l.plugin.getTitle()+"</b> "+l.plugin.getDescription()+"</html>");
            } else {
                setText("<html><b style=\"color: #000055\">["+l.plugin.getTitle()+"]</b> "+l.plugin.getDescription()+"</html>");
            }
            setSelected(l.enabled);
            setEnabled(list.isEnabled());

            if (isSelected || cellHasFocus) {
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            } else {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }

            return this;
        }
        
    }
    
    /** Creates new form Main */
    public GUI() {
        initComponents();
        setLocationRelativeTo(null);

        jTabbedPane1.add("Errors / Warnings",ErrorCollector.getPresenter());

        jList1.setModel(new DefaultListModel());
        jList1.setCellRenderer(new KarbonPluginListRenderer());
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jList1.addMouseListener(new MouseAdapter() {
            @Override
             public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();
                if (!list.isEnabled()) return;

                // Get index of item clicked

                int index = list.locationToIndex(event.getPoint());
                PluginInstance item = (PluginInstance)
                   list.getModel().getElementAt(index);

                // Toggle selected state
                if (item.enabled) {
                    Main.pluginDB.disablePlugin(item);
                } else {
                    try {
                        Main.pluginDB.enablePlugin(item);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(rootPane, "Error while loading plugin "+item.name+"! "+ex.getMessage(), "Initialization error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                // Repaint cell
                list.repaint(list.getCellBounds(index, index));
             }
          });

        // Load the app settings
        jFilename.setText( Main.properties.getProperty("filename", jFilename.getText() ) );
        jTCPPort.setText( Main.properties.getProperty("tcpport", jTCPPort.getText()) );
        jUDPPort.setText( Main.properties.getProperty("udpport", jUDPPort.getText()) );
        jTCPBufferText.setText( Main.properties.getProperty("tcpbuffer", jTCPBufferText.getText()) );
        jUDPBufferText.setText( Main.properties.getProperty("udpbuffer", jUDPBufferText.getText()) );

        // Hook on plugin DB
        final GUI me = this;
        Main.pluginDB.addPluginEventHandler(new PluginDB.PluginEventHandler() {
            @Override
            public void pluginLoaded(PluginInstance p) {
                DefaultListModel m = (DefaultListModel) jList1.getModel();
                m.addElement(p);
            }

            @Override
            public void pluginUnLoaded(PluginInstance p) {
                DefaultListModel m = (DefaultListModel) jList1.getModel();
                m.removeElement(p);
            }

            @Override
            public void pluginEnabled(PluginInstance p) {
                jList1.repaint();
            }

            @Override
            public void pluginDisabled(PluginInstance p) {
                jList1.repaint();
            }
        });
        Main.pluginDB.addPresenterEventHandler(new PluginDB.PresenterEventHandler() {
            @Override
            public void presenterAdded(TPresenter p) {
                jTabbedPane1.add(p.Title, p);
            }

            @Override
            public void presenterRemoved(TPresenter p) {
                jTabbedPane1.remove(p);
            }
        });


    }

    public void GUIAbortAnalysis() {
        this.setTitle("Karbon - Analysis aborted");
        jTabbedPane2.setEnabled(true);
        jStartAnalysis.setEnabled(true);
        jList1.setEnabled(true);
        jAbortAnalysis.setEnabled(false);
        jButton2.setEnabled(true);
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        worker.firePropertyChange("forced_cancel", 0, 1);
        worker.cancel(true);
        GUICompleted();
        jStatusLabel.setText("Analysis aborted by user");
    }

    public void GUICompleted() {
        this.setTitle("Karbon - Analysis completed");
        jStatusLabel.setText("Completed");
        jList1.setEnabled(true);
        jStartAnalysis.setEnabled(true);
        jAbortAnalysis.setEnabled(false);
        jTabbedPane2.setEnabled(true);
        jButton2.setEnabled(true);
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        jProgressBar1.setValue(0);
        jSpeed.setText("0 lines/sec");
        //updateReport();
    }

    public void GUIStartProcess(TStream stream) {

        // Set GUI in analyzing state
        jTabbedPane2.setEnabled(false);
        jStartAnalysis.setEnabled(false);
        jList1.setEnabled(false);
        jAbortAnalysis.setEnabled(true);
        jButton2.setEnabled(false);
        jButton3.setEnabled(false);
        jButton4.setEnabled(false);
        jStatusLabel.setText("Analyzing...");

        // Reset errors
        ErrorCollector.reset();

        // Start worker
        worker = Main.processor.startAnalysis(stream);

        // Bind updates
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
          public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            String property = propertyChangeEvent.getPropertyName();
            if ("progress".equals(property)) {
                jProgressBar1.setValue((Integer)propertyChangeEvent.getNewValue());
            } else if ("speed".equals(property)) {
                jSpeed.setText(String.valueOf(((Double)propertyChangeEvent.getNewValue()).intValue())+ " lines/sec");
            } else if ("finished".equals(property)) {
                GUICompleted();
            }
          }
        };
        worker.addPropertyChangeListener(listener);
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
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jBrowsePanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jFilename = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jUDPPort = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jUDPBufferText = new javax.swing.JTextField();
        jUDPBufferCheck = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTCPPort = new javax.swing.JTextField();
        jTCPBufferCheck = new javax.swing.JCheckBox();
        jTCPBufferText = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLogText = new javax.swing.JTextArea();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jProgressPanel = new javax.swing.JPanel();
        jSpeed = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jStatusLabel = new javax.swing.JLabel();
        jStartAnalysis = new javax.swing.JButton();
        jAbortAnalysis = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Karbon - Application profiler based on trace filles");

        jPanel1.setPreferredSize(new java.awt.Dimension(767, 200));

        jLabel4.setText("Select a file to parse:");

        jFilename.setText("C:\\Temp\\trace\\timestamp\\btrace.dmp");
        jFilename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFilenameActionPerformed(evt);
            }
        });

        jButton1.setText("Browse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jBrowsePanelLayout = new javax.swing.GroupLayout(jBrowsePanel);
        jBrowsePanel.setLayout(jBrowsePanelLayout);
        jBrowsePanelLayout.setHorizontalGroup(
            jBrowsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBrowsePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jBrowsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jBrowsePanelLayout.createSequentialGroup()
                        .addComponent(jFilename, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addContainerGap())
        );
        jBrowsePanelLayout.setVerticalGroup(
            jBrowsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBrowsePanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jBrowsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(199, 199, 199))
        );

        jTabbedPane2.addTab("From File", jBrowsePanel);

        jLabel2.setText("Listen to port:");

        jUDPPort.setText("1225");

        jButton7.setText("Browse");
        jButton7.setEnabled(false);

        jUDPBufferText.setText("buffer-udp.dump");
        jUDPBufferText.setEnabled(false);

        jUDPBufferCheck.setText("Save input buffer to:");
        jUDPBufferCheck.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jUDPPort, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jUDPBufferCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jUDPBufferText, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jUDPPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUDPBufferText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUDPBufferCheck))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("From UDP Stream", jPanel2);

        jLabel3.setText("Listen to port:");

        jTCPPort.setText("1225");

        jTCPBufferCheck.setText("Save input buffer to:");
        jTCPBufferCheck.setEnabled(false);

        jTCPBufferText.setText("buffer-tcp.dump");
        jTCPBufferText.setEnabled(false);

        jButton8.setText("Browse");
        jButton8.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTCPPort, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTCPBufferCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTCPBufferText, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTCPPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTCPBufferText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTCPBufferCheck))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("From TCP Stream", jPanel4);

        jButton9.setText("Open live trace manager");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jLabel5.setForeground(new java.awt.Color(102, 102, 102));
        jLabel5.setText("Last trace file:");

        jLabel6.setText("(None)");
        jLabel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jButton10.setText("Save...");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                            .addComponent(jButton10))))
                .addContainerGap())
        );

        jTabbedPane2.addTab("New trace", jPanel5);

        jList1.setSelectionBackground(new java.awt.Color(153, 153, 255));
        jScrollPane2.setViewportView(jList1);

        jLabel1.setText("Select plugins to use:");

        jButton2.setText("All");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("None");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Load JAR file...");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 723, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton2)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Analyzer", jPanel1);

        jLogText.setColumns(20);
        jLogText.setFont(new java.awt.Font("Monospaced", 0, 12));
        jLogText.setRows(5);
        jScrollPane1.setViewportView(jLogText);

        jButton5.setText("Update");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Save report");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Collected Report", jPanel3);

        jSpeed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jSpeed.setText("0 lines/sec");

        jStatusLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        jStatusLabel.setText("Specify a filename or a network source to start analysis");

        jStartAnalysis.setText("Start");
        jStartAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartAnalysisActionPerformed(evt);
            }
        });

        jAbortAnalysis.setText("Abort");
        jAbortAnalysis.setEnabled(false);
        jAbortAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAbortAnalysisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jProgressPanelLayout = new javax.swing.GroupLayout(jProgressPanel);
        jProgressPanel.setLayout(jProgressPanelLayout);
        jProgressPanelLayout.setHorizontalGroup(
            jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jStartAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAbortAnalysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jProgressPanelLayout.createSequentialGroup()
                        .addComponent(jStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 243, Short.MAX_VALUE)
                        .addComponent(jSpeed))
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE))
                .addContainerGap())
        );
        jProgressPanelLayout.setVerticalGroup(
            jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jProgressPanelLayout.createSequentialGroup()
                .addGroup(jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jProgressPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jStartAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jAbortAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jProgressPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpeed)
                            .addComponent(jStatusLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jProgressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 995, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(jProgressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jFilenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFilenameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFilenameActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();
        int ret = fc.showDialog(this, "Select");
        if (ret == JFileChooser.APPROVE_OPTION) {
            jFilename.setText(fc.getSelectedFile().toString());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String errors = "";
        for (int i=0; i<Main.pluginDB.plugins.size(); i++) {
            try {
                Main.pluginDB.enablePlugin(Main.pluginDB.plugins.get(i));
            } catch (Exception ex) {
                errors += "Error while loading "+Main.pluginDB.plugins.get(i).name+": "+ex.getMessage()+"\n";
            }
        }
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, errors, "Initialization error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        for (int i=0; i<Main.pluginDB.plugins.size(); i++) {
            Main.pluginDB.disablePlugin(Main.pluginDB.plugins.get(i));
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jStartAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartAnalysisActionPerformed
        Main.properties.setProperty("filename", jFilename.getText());
        Main.properties.setProperty("tcpport", jTCPPort.getText());
        Main.properties.setProperty("udpport", jUDPPort.getText());
        Main.properties.setProperty("tcpbuffer", jTCPBufferText.getText());
        Main.properties.setProperty("udpbuffer", jUDPBufferText.getText());
        Main.saveSettings();

        if (jTabbedPane2.getSelectedIndex() == 0) {
            if (jFilename.getText().equals("")) {
                JOptionPane.showMessageDialog(rootPane, "Please specify a filename to use!", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                if ( new File(jFilename.getText()).exists() ) {

                    String displayName = jFilename.getText();
                    if (displayName.length() > 128) {
                        displayName = "..." + jFilename.getText().substring(displayName.length() - 125);
                    }
                    this.setTitle("Karbon - Profiling "+displayName);
                    GUIStartProcess(new FileStream(jFilename.getText()));
                } else {
                    JOptionPane.showMessageDialog(rootPane, "The specified filename does not exist!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (jTabbedPane2.getSelectedIndex() == 1) {
            if (jUDPPort.getText().isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "Please specify a port to use!", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    Integer port = Integer.parseInt(jUDPPort.getText());
                    this.setTitle("Karbon - Profiling data on UDP Port " + port);
                    GUIStartProcess(new UDPStream(port));
                } catch (SocketException ex) {
                    JOptionPane.showMessageDialog(rootPane, "Unable to start the UDP Server! "+ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (jTabbedPane2.getSelectedIndex() == 2) {
            if (jTCPPort.getText().isEmpty()) {
                JOptionPane.showMessageDialog(rootPane, "Please specify a port to use!", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    Integer port = Integer.parseInt(jTCPPort.getText());
                    this.setTitle("Karbon - Profiling data on TCP Port " + port);
                    GUIStartProcess(new TCPStream(port));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(rootPane, "Unable to start the TCP Server! "+ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (jTabbedPane2.getSelectedIndex() == 3) {
            if (jLabel6.getText().equals("(None)")) {
                LiveTracerDialog l = new LiveTracerDialog(this, true);
                l.setVisible(true);
                jLabel6.setText(l.getFilename());
            } else {
                if ( new File(jLabel6.getText()).exists() ) {

                    String displayName = jLabel6.getText();
                    if (displayName.length() > 128) {
                        displayName = "..." + jLabel6.getText().substring(displayName.length() - 125);
                    }
                    this.setTitle("Karbon - Profiling "+displayName);
                    GUIStartProcess(new FileStream(jLabel6.getText()));
                } else {
                    JOptionPane.showMessageDialog(rootPane, "A tracer file was not created or cannot be accessed!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

    }//GEN-LAST:event_jStartAnalysisActionPerformed

    private void jAbortAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAbortAnalysisActionPerformed
        GUIAbortAnalysis();
    }//GEN-LAST:event_jAbortAnalysisActionPerformed

    public void updateReport() {
        jLogText.setText(Main.processor.generateReport(ReportFormat.Text));
    }

    public void saveReport() {

        IReportable reportCollector = new IReportable() {

            @Override
            public String getReport(ReportFormat format) {
                return Main.processor.generateReport(format);
            }

            @Override
            public String getTitle() {
                return "[ Summary Report ]";
            }

            @Override
            public boolean includeToReport() {
                return true;
            }
        };

        ReportTools.saveReportDialog(reportCollector);

    }
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        updateReport();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        saveReport();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            File myJar = ReportTools.openDialog("Browse jar file", new Object[][] { { "JAR file", new String[] { ".jar" } } });
            if (myJar == null) return;
            Main.pluginDB.loadPluginsFromJAR(myJar);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error loading the specified JAR file! " + ex.getMessage(), "JAR Error", JOptionPane.WARNING_MESSAGE);
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        LiveTracerDialog l = new LiveTracerDialog(this, true);
        l.setVisible(true);
        jLabel6.setText(l.getFilename());
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        String filename = ReportTools.saveAsDialog("Save report as...", new Object[][]{{"Trace files", new String[]{".trace"}}});
        if (filename.isEmpty()) return;
        try {
            new File(jLabel6.getText()).renameTo(new File(filename));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error while moving the specified file! " + ex.getMessage(), "JAR Error", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    /**
    * @param args the command line arguments
    */
    /*
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
     *
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAbortAnalysis;
    public javax.swing.JPanel jBrowsePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JTextField jFilename;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JTextArea jLogText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar1;
    public javax.swing.JPanel jProgressPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jSpeed;
    private javax.swing.JButton jStartAnalysis;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JCheckBox jTCPBufferCheck;
    private javax.swing.JTextField jTCPBufferText;
    private javax.swing.JTextField jTCPPort;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JCheckBox jUDPBufferCheck;
    private javax.swing.JTextField jUDPBufferText;
    private javax.swing.JTextField jUDPPort;
    // End of variables declaration//GEN-END:variables

}

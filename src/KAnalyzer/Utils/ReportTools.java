/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.API.IReportable;
import KAnalyzer.ErrorCollector;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class ReportTools {

    public static final String HTML_HEAD = "<html><head><title>HTML Report</title></head><body><h1>HTML Report</h1>\n";
    public static final String HTML_FOOTER = "\n</body></html>\n";

    public static enum ReportFormat {
        Text, CSV, HTML
    }

    /*
     * A class to generate reports (Not uset yet)
     * @unused
     */
    public static class ReportGenerator {
        
        private String buffer = "";

        public ReportGenerator(ReportFormat format) {
            buffer = "";
            
        }

        public void setFixes(String globalHead, String itemHead, String itemFooter, String globalFooter) {
            
        }

        public void setTitle(String title) {
            
        }

        public void addItem(String data) {
            
        }

        public String getBuffer() {
            return "";
        }

    }

    public static void saveReportDialog(IReportable ofPresenter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save report " + ofPresenter.getTitle());
        chooser.addChoosableFileFilter( new ExtensionFilter("HTML Report", new String[] { ".htm", ".html" }));
        chooser.addChoosableFileFilter( new ExtensionFilter("CSV Report", "csv"));
        chooser.addChoosableFileFilter( new ExtensionFilter("Text Report", new String[] { ".txt", ".log" }));
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setMultiSelectionEnabled(false);
        int result = chooser.showDialog(chooser, "Save report");
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.exists()) {
                if (!f.canWrite()) {
                    JOptionPane.showMessageDialog(null, "Cannot write to "+f.getAbsolutePath()+"!", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            FileWriter fw;
            try {
                String filename = f.getPath();
                fw = new FileWriter(filename, false);
                if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                    fw.write(ofPresenter.getReport(ReportFormat.HTML));
                } else if (filename.endsWith(".csv")) {
                    fw.write(ofPresenter.getReport(ReportFormat.CSV));
                } else {
                    fw.write(ofPresenter.getReport(ReportFormat.Text));
                }
                fw.close();
            } catch (IOException ex) {
                ErrorCollector.store_exception(ex, "GridPresenterEx.jButton1ActionPerformed", false);
            }
        }
    }

    public static String saveAsDialog(String Title, Object[][] extensions) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Title);

        for (Object[] o: extensions ) {
            chooser.addChoosableFileFilter( new ExtensionFilter((String)o[0], (String[])o[1]));
        }
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setMultiSelectionEnabled(false);
        
        int result = chooser.showDialog(chooser, "Save");
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.exists()) {
                if (!f.canWrite()) {
                    JOptionPane.showMessageDialog(null, "Cannot write to "+f.getAbsolutePath()+"!", "Error", JOptionPane.WARNING_MESSAGE);
                    return "";
                }
            }
            return f.getPath();
        }
        return "";
    }

    public static File openDialog(String Title, Object[][] extensions) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Title);

        for (Object[] o: extensions ) {
            chooser.addChoosableFileFilter( new ExtensionFilter((String)o[0], (String[])o[1]));
        }
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setMultiSelectionEnabled(false);

        int result = chooser.showDialog(chooser, "Open");
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.exists()) {
                JOptionPane.showMessageDialog(null, "Cannot find the specified file!", "Error", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return f;
        }
        return null;
    }

    public static void snapshotComponentToJPEG(Component myComponent, String filename) {
        BufferedImage bufImage = new BufferedImage(myComponent.getSize().width, myComponent.getSize().height,BufferedImage.TYPE_INT_RGB);
        myComponent.paint(bufImage.createGraphics());
        File imageFile = new File(filename);
        try{  
            imageFile.createNewFile();  
            ImageIO.write(bufImage, "jpeg", imageFile);  
        }catch(Exception ex){
            ErrorCollector.store_exception(ex, "ReportTools.snapshotComponentToJPEG", false);
        }  
        
    }

    /*
    public static void snapshotComponentToJPEG_v2(Component myComponent, String filename) {
       Dimension size = myComponent.getSize();
       BufferedImage myImage =
         new BufferedImage(size.width, size.height,
         BufferedImage.TYPE_INT_RGB);
       Graphics2D g2 = myImage.createGraphics();
       myComponent.paint(g2);
       try {
         OutputStream out = new FileOutputStream(filename);
         JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
         encoder.encode(myImage);
         out.close();
       } catch (Exception e) {
         ErrorCollector.store_exception(e, "ReportTools.snapshotComponentToJPEG");
       }
     }
     */

}

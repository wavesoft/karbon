/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon;

import KAnalyzer.ErrorCollector;
import karbon.Plugins.*;
import KAnalyzer.Utils.FileStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import karbon.Plugins.ProcTracker;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class Main {

    public static Processor processor;
    public static boolean UseGUI;
    public static Properties properties;
    public static PluginDB pluginDB;

    private static void NoGUIStartProcess(String filename) {
        SwingWorker w = processor.startAnalysis(new FileStream(filename));
        try {
            w.wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void saveSettings() {
        try {
            properties.storeToXML(new FileOutputStream("settings.xml"), null);
        } catch (FileNotFoundException ex) {
            ErrorCollector.store_exception(ex, "Main.saveSettings", false);
        } catch (IOException ex) {
            ErrorCollector.store_exception(ex, "Main.saveSettings", false);
        }
    }

    public static void loadPlugins() {
        try {
            pluginDB.loadPlugin(new PTProcessLookup());
            pluginDB.loadPlugin(new PTCallClassifier());
            pluginDB.loadPlugin(new SysCalls());
            pluginDB.loadPlugin(new SocketTracker());
            pluginDB.loadPlugin(new FileTracker());
            pluginDB.loadPlugin(new HotspotTracker());
            pluginDB.loadPlugin(new ProcTracker());
            pluginDB.loadPlugin(new TimeUtilization());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loadJAR(File myJar) throws Exception {
        ClassLoader c = new URLClassLoader(new URL[]{myJar.toURI().toURL()});
        Class classToLoad = Class.forName("com.MyClass", true, c);
        Method method = classToLoad.getDeclaredMethod("myMethod");
        Object instance = classToLoad.newInstance();
        Object result = method.invoke(instance);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream("settings.xml"));
        } catch (FileNotFoundException ex) {
            ErrorCollector.store_exception(ex, "Main.main", false);
        } catch (IOException ex) {
            ErrorCollector.store_exception(ex, "Main.main", false);
        }
        /*
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                    new com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException e) {
           // handle exception
        }
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        processor = new Processor();
        pluginDB = new PluginDB(processor);

        /*
        processor.registerTool(new LCallClassifier());
        processor.registerTool(new LProcLookup());
        processor.registerPlugin(new KCalls(), new PassthruRule());
        processor.registerPlugin(new KProcessTracker(), new PassthruRule());
        processor.registerPlugin(new KHotspotTracker(), new PassthruRule());
        processor.registerPlugin(new KFileTracker(), new VoidRule());
        processor.registerPlugin(new KNetworkTracker(), new VoidRule());
        processor.registerPlugin(new KTimeUtilization(), new VoidRule());
        processor.registerPlugin(new KOverallTime(), new PassthruRule());
         */

        UseGUI = true;
        String f_source = "";
        Console c = System.console();
        if (c == null) UseGUI = true;
        
        if (UseGUI) {
            GUI gui = new GUI();
            loadPlugins();
            gui.setVisible(true);

            if (!f_source.equals("")) {
                gui.GUIStartProcess(new FileStream(f_source));
            } else {
                gui.jBrowsePanel.setVisible(true);
            }
            
        } else {
            c.printf("KARBON Trace File Analyzer  ver 1.0\n\n");

            if (!f_source.equals("")) {
                NoGUIStartProcess(f_source);
            } else {
                c.printf("Please specify a filename to analyze!\n");
            }
        }

    }
    
}

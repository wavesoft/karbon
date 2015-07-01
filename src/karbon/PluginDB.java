/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.API.TPreprocessingTool;
import KAnalyzer.API.TPresenter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author John
 */
public class PluginDB {

    public interface PresenterEventHandler {
        public void presenterAdded(TPresenter p);
        public void presenterRemoved(TPresenter p);
    }

    public interface PluginEventHandler {
        public void pluginLoaded(PluginInstance p);
        public void pluginUnLoaded(PluginInstance p);
        public void pluginEnabled(PluginInstance p);
        public void pluginDisabled(PluginInstance p);
    }

    private static int last_id = 0;

    public enum PlugintType {
        PLUGIN_PREPROCESSOR, PLUGIN_ANALYZER
    }

    public class PluginInstance {
        public IKarbonPlugin        plugin = null;
        public Object               targetInstance = null;
        public IAnalyzeRule         ruleInstance = null;
        public TPresenter           presenterInstance = null;
        public Boolean              ready = false;
        public PlugintType          type = PlugintType.PLUGIN_ANALYZER;
        public Boolean              enabled = false;

        public int                  id = 0;
        public String               name = "";

        public PluginInstance(IKarbonPlugin plugin) throws Exception {
            // Initialize plugin and instances
            this.plugin = plugin;
            if (plugin == null) throw new NullPointerException("Plugin was null!");
            this.targetInstance = plugin.instanceTarget();
            if (targetInstance == null) throw new NullPointerException("TargetInstance was null!");
            this.ruleInstance = plugin.instanceRule();
            if (ruleInstance == null) throw new NullPointerException("RuleInstance was null!");
            this.name = plugin.getClass().getName();
            this.id = ++last_id;

            // (Try to) Instance target
            if (this.targetInstance instanceof TAnalyzeTarget) {
                this.presenterInstance = ((TAnalyzeTarget)this.targetInstance).getPresenter();
                this.type = PlugintType.PLUGIN_ANALYZER;
            } else if (this.targetInstance instanceof TPreprocessingTool) {
                this.presenterInstance = ((TPreprocessingTool)this.targetInstance).getPresenter();
                this.type = PlugintType.PLUGIN_PREPROCESSOR;
            } else {
                throw new Exception("The plugin instanced a target that was not known!");
            }
        }

    }

    ArrayList<PluginInstance>               plugins = new ArrayList<PluginInstance>();
    ArrayList<PluginEventHandler>           evPlugin = new ArrayList<PluginEventHandler>();
    ArrayList<PresenterEventHandler>        evPresenter = new ArrayList<PresenterEventHandler>();
    Processor                               processor;

    public void addPluginEventHandler(PluginEventHandler h) {
        evPlugin.add(h);
    }

    public void addPresenterEventHandler(PresenterEventHandler h) {
        evPresenter.add(h);
    }

    private void notifyPluginLoaded(PluginInstance p) {
        for (PluginEventHandler h: evPlugin) {
            h.pluginLoaded(p);
        }
    }

    private void notifyPluginUnloaded(PluginInstance p) {
        for (PluginEventHandler h: evPlugin) {
            h.pluginUnLoaded(p);
        }
    }

    private void notifyPluginEnabled(PluginInstance p) {
        for (PluginEventHandler h: evPlugin) {
            h.pluginEnabled(p);
        }
    }

    private void notifyPluginDisabled(PluginInstance p) {
        for (PluginEventHandler h: evPlugin) {
            h.pluginDisabled(p);
        }
    }

    private void notifyPresenterAdded(TPresenter p) {
        for (PresenterEventHandler h: evPresenter) {
            h.presenterAdded(p);
        }
    }

    private void notifyPresenterRemoved(TPresenter p) {
        for (PresenterEventHandler h: evPresenter) {
            h.presenterRemoved(p);
        }
    }

    public PluginDB(Processor processor) {
        this.processor = processor;
    }


    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @SuppressWarnings("unchecked") 
    private static List<Class> getClasses(String packageName, ClassLoader classLoader)
            throws ClassNotFoundException, IOException  {
        
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String fileName = resource.getFile();
            String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
            dirs.add(new File(fileNameDecoded));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException  {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
        	String fileName = file.getName();
            if (file.isDirectory()) {
                assert !fileName.contains(".");
            	classes.addAll(findClasses(file, packageName + "." + fileName));
            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
            	Class _class;
                    try {
                            _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
                    } catch (ExceptionInInitializerError e) {
                            // happen, for example, in classes, which depend on
                            // Spring to inject some beans, and which fail,
                            // if dependency is not fulfilled
                            _class = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6),
                                            false, Thread.currentThread().getContextClassLoader());
                    }
                    classes.add(_class);
            }
        }
        return classes;
    }

    public boolean loadPluginsFromJAR(File jarFile) {
        try {
            ClassLoader c = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});

            // Fetch the base name of the file, without the extension
            String packageName = jarFile.getName();
            packageName = packageName.substring(0, packageName.length()-4);

            List<Class> loadedClasses = getClasses(packageName, c);

            // Look for classes inside a package with the same name as the file
            Enumeration<URL> resources = c.getResources(packageName+"/");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String fileName = resource.getFile();
                fileName = URLDecoder.decode(fileName, "UTF-8");
                File f = new File(fileName);
                
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    Class classToLoad;
                    try {
                        classToLoad = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
                    } catch (ExceptionInInitializerError e) {
                        // happen, for example, in classes, which depend on
                        // Spring to inject some beans, and which fail,
                        // if dependency is not fulfilled
                        classToLoad = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6),
                                    false, Thread.currentThread().getContextClassLoader());
                    }

                    if (classToLoad.isInstance(IKarbonPlugin.class)) {
                        IKarbonPlugin p = (IKarbonPlugin) classToLoad.newInstance();
                        loadPlugin(p);
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to load the specified JAR file! "+ex.getMessage(), "Loader error", JOptionPane.ERROR_MESSAGE);
        }
        return true;
    }

    public int loadPlugin(IKarbonPlugin p) throws Exception {
        for (PluginInstance pi: plugins) {
            if (pi.plugin.getClass().equals(p.getClass())) {
                return pi.id;
            }
        }
        PluginInstance i = new PluginInstance(p);
        plugins.add(i);
        notifyPluginLoaded(i);
        return i.id;
    }

    public Boolean enablePlugin(IKarbonPlugin plugin) throws Exception {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.plugin.equals(plugin)) {
                //plugins.remove(i);
                //plugins.add(p);
                return enablePlugin(p);
            }
        }
        return false;
    }

    public Boolean enablePlugin(Integer id) throws Exception {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.id == id) {
                //plugins.remove(i);
                //plugins.add(p);
                return enablePlugin(p);
            }
        }
        return false;
    }
    
    public Boolean enablePlugin(String className) throws Exception {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.name.equals(className)) {
                //plugins.remove(i);
                //plugins.add(p);
                return enablePlugin(p);
            }
        }
        return false;
    }

    public Boolean enablePlugin(PluginInstance i) throws Exception {
        if (i.enabled) return true;

        // Try to enable all of it's dependencies
        String[] deps = i.plugin.getRequiredPlugins();
        if (deps != null) {
            for (String pl: deps) {
                if (!enablePlugin(pl))
                    throw new Exception("Unable to find a class that this plugin depends on: "+pl);
            }
        }

        // Then enable plugin
        if (i.type == PlugintType.PLUGIN_ANALYZER) {
            processor.router.addTarget((TAnalyzeTarget) i.targetInstance,i.ruleInstance);
        } else if (i.type == PlugintType.PLUGIN_PREPROCESSOR) {
            processor.router.addTool((TPreprocessingTool) i.targetInstance);
        }
        if (i.presenterInstance != null) {
            processor.presenters.add(i.presenterInstance);
            notifyPresenterAdded(i.presenterInstance);
        }
        i.enabled = true;
        notifyPluginEnabled(i);
        return true;
    }

    public Boolean disablePlugin(IKarbonPlugin plugin) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.plugin.equals(plugin)) {
                plugins.remove(i);
                plugins.add(p);
                return disablePlugin(p);
            }
        }
        return false;
    }

    public Boolean disablePlugin(Integer id) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.id == id) {
                plugins.remove(i);
                plugins.add(p);
                return disablePlugin(p);
            }
        }
        return false;
    }

    public Boolean disablePlugin(String className) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.name.equals(className)) {
                plugins.remove(i);
                plugins.add(p);
                return disablePlugin(p);
            }
        }
        return false;
    }

    public Boolean disablePlugin(PluginInstance i) {
        if (!i.enabled) return true;
        if (i.type == PlugintType.PLUGIN_ANALYZER) {
            processor.router.removeTarget((TAnalyzeTarget) i.targetInstance,i.ruleInstance);
        } else if (i.type == PlugintType.PLUGIN_PREPROCESSOR) {
            processor.router.removeTool((TPreprocessingTool) i.targetInstance);
        }
        if (i.presenterInstance != null) {
            processor.presenters.remove(i.presenterInstance);
            notifyPresenterRemoved(i.presenterInstance);
        }
        i.enabled = false;
        notifyPluginDisabled(i);
        return true;
    }

    public void unloadPlugin(String className) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.name.equals(className)) {
                unloadPlugin(p);
                break;
            }
        }
    }

    public void unloadPlugin(Integer id) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.id == id) {
                unloadPlugin(p);
                break;
            }
        }
    }

    public void unloadPlugin(IKarbonPlugin plugin) {
        PluginInstance p;
        for (int i=0; i<plugins.size(); i++) {
            p = plugins.get(i);
            if (p.plugin.equals(plugin)) {
                unloadPlugin(p);
                break;
            }
        }
    }

    private void unloadPlugin(PluginInstance i) {
        if (i.enabled) disablePlugin(i);
        plugins.remove(i);
        notifyPluginUnloaded(i);
    }

}

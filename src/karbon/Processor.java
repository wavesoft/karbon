/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon;

import KAnalyzer.Utils.*;
import KAnalyzer.API.*;
import KAnalyzer.*;
import KAnalyzer.Interfaces.TStream;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.SwingWorker;

/**
 *
 * This is the off-screen processor class
 * 
 * This processor can be invoked either from command line or from the
 * GUI and it will work the same way.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class Processor {

    public Router router;
    public TTokenizer tokenizer;
    public ArrayList<KAnalyzer.API.TPresenter> presenters;

    public Processor() {
        tokenizer = new DefaultTokenizer();
        router = new Router(tokenizer);
        presenters = new ArrayList<KAnalyzer.API.TPresenter>();
    }

    public void registerTool(TPreprocessingTool tool) {
        TPresenter p = tool.getPresenter();
        if (p != null) presenters.add(p);
        router.addTool(tool);
    }

    public TPresenter registerPlugin(TAnalyzeTarget target, IAnalyzeRule rule) {
        TPresenter p = target.getPresenter();
        router.addTarget(target, rule);
        presenters.add(p);
        return p;
    }

    public String generateReport(ReportTools.ReportFormat format) {
        String buffer = "";
        TPresenter errors = ErrorCollector.getPresenter();
        if (errors.includeToReport()) buffer += errors.getReport(format)+"\n";
        for (TPresenter p : Main.processor.presenters) {
            if (p.includeToReport()) {
                buffer += p.getReport(format)+"\n";
            }
        }
        return buffer;
    }

    /*
     * TODO Implement this
     */
    public TPresenter installPlugin(String JARFile) {
        ClassLoader c = ClassLoader.getSystemClassLoader();
        try {
            c.loadClass(JARFile);
        } catch (ClassNotFoundException ex) {
            KAnalyzer.ErrorCollector.store_exception(ex, "Processor.installPlugin", true);
        }

        return null;
    }

    /*public SwingWorker AnalyzeFile(String filename) {
        router.resetToolsAndTargets();
        router.broadcastBeginAnalysis();
        FileStream fs = new FileStream(filename);
        fs.addReceiver(router);
        SwingWorker worker = fs.getAsyncWorker();
        worker.execute();
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if ("finished".matches(name)) {
                    if ((Integer)evt.getNewValue() == 1) router.broadcastCompleteAnalysis();
                }
            }
        });
        return worker;
    }*/

    public SwingWorker startAnalysis(TStream source) {
        router.resetToolsAndTargets();
        router.broadcastBeginAnalysis();
        source.addReceiver(router);
        SwingWorker w = source.getAsyncWorker();
        w.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if ("finished".matches(name)) {
                    if ((Integer)evt.getNewValue() == 1) router.broadcastCompleteAnalysis();
                }
            }
        });
        w.execute();
        return w;
    }

}

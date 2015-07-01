/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import KAnalyzer.Utils.VoidRule;
import karbon.Analyzing.KFileTracker;

/**
 *
 * @author John
 */
public class FileTracker implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KFileTracker();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new VoidRule();
    }

    @Override
    public String getTitle() {
        return "File Tracker";
    }

    @Override
    public String getDescription() {
        return "Detects what files were being accessed and what are their I/O statistics";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTCallClassifier"
        };
    }

}

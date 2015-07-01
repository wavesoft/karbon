/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import karbon.Analyzing.KProcessTracker;

/**
 *
 * @author John
 */
public class ProcTracker implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KProcessTracker();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new PassthruRule();
    }

    @Override
    public String getTitle() {
        return "Process Tracker";
    }

    @Override
    public String getDescription() {
        return "Tracks how the processes evolve through time. Also builds a process lifetime diagram";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
        };
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import karbon.Analyzing.KCalls;

/**
 *
 * @author John
 */
public class SysCalls implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KCalls();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new PassthruRule();
    }

    @Override
    public String getTitle() {
        return "System Calls";
    }

    @Override
    public String getDescription() {
        return "List details of the system calls used system wide and per-process";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTProcessLookup"
        };
    }

}

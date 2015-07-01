/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.VoidRule;
import karbon.Analyzing.KTimeUtilization;

/**
 *
 * @author John
 */
public class TimeUtilization implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KTimeUtilization();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new VoidRule();
    }

    @Override
    public String getTitle() {
        return "Time Utilization";
    }

    @Override
    public String getDescription() {
        return "Displays the sub-time time of various operations that were performed";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTCallClassifier"
        };
    }

}

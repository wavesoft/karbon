/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import karbon.Analyzing.KHotspotTracker;

/**
 *
 * @author John
 */
public class HotspotTracker implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KHotspotTracker();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new PassthruRule();
    }

    @Override
    public String getTitle() {
        return "Hotspot Tracker";
    }

    @Override
    public String getDescription() {
        return "Helps detect the most time-consuming information";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTCallClassifier",
            "karbon.Plugins.PTProcessLookup"
        };
    }

}

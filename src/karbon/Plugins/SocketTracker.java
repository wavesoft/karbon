/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.VoidRule;
import karbon.Analyzing.KNetworkTracker;

/**
 *
 * @author John
 */
public class SocketTracker implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new KNetworkTracker();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new VoidRule();
    }

    @Override
    public String getTitle() {
        return "Socket Tracker";
    }

    @Override
    public String getDescription() {
        return "Tracks all the incoming and outgoing socket connections. Includes UNIX Sockets";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTCallClassifier"
        };
    }

}

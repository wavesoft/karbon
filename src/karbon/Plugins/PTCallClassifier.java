/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import java.util.List;
import karbon.Analyzing.LCallClassifier;

/**
 *
 * @author John
 */
public class PTCallClassifier implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new LCallClassifier();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new PassthruRule();
    }

    @Override
    public String getTitle() {
        return "SysCall Classifier";
    }

    @Override
    public String getDescription() {
        return "A preprocessing tool that classifies the system calls for easier and faster further processing";
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[] {
            "karbon.Plugins.PTProcessLookup"
        };
    }

}

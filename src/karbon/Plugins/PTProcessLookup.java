/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Plugins;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.IKarbonPlugin;
import KAnalyzer.Utils.PassthruRule;
import java.util.List;
import karbon.Analyzing.LProcLookup;

/**
 *
 * @author John
 */
public class PTProcessLookup implements IKarbonPlugin {

    @Override
    public Object instanceTarget() {
        return new LProcLookup();
    }

    @Override
    public IAnalyzeRule instanceRule() {
        return new PassthruRule();
    }

    @Override
    public String getTitle() {
        return "Process Lookup";
    }

    @Override
    public String getDescription() {
        return "A preprocessing tool that tries to map all the PIDs to their appropriate images/command line arguments";
    }

    @Override
    public String[] getRequiredPlugins() {
        return null;
    }

}

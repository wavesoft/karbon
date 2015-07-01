/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.API;

import java.util.List;

/**
 *
 * @author John
 */
public interface IKarbonPlugin {

    public Object           instanceTarget();
    public IAnalyzeRule     instanceRule();
    public String           getTitle();
    public String           getDescription();
    public String[]         getRequiredPlugins();
    
}

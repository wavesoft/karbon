/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Interfaces;

import KAnalyzer.ErrorCollector;
import java.util.Hashtable;

/**
 *
 * The ToolBus is a variable environment used by the pre-processing tools
 * 
 * The pre-processing tools create various variables here, that can be extracted
 * by the TargetAnalyzers.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class TToolBus {

    public Hashtable<String, Object> toolParam;

    public TToolBus() {
        toolParam = new Hashtable<String, Object>();
    }

    public void reset() {
        toolParam.clear();
    }

    public void set(String what, Object toWhat) {
        try {
            if (toWhat == null) {
                toolParam.put(what, false);
            } else {
                toolParam.put(what, toWhat);
            }
        } catch (NullPointerException ex) {
            ErrorCollector.store_exception(ex, "TLineToken.set", false);
        }
    }

    public boolean containsKey(String what) {
        return toolParam.containsKey(what);
    }

    public Object get(String what) {
        return toolParam.get(what);
    }

}

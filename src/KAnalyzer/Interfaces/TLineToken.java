/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Interfaces;

import KAnalyzer.ErrorCollector;
import java.util.Hashtable;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class TLineToken {

    public Hashtable<String, Object> arguments;
    public String fullLine;
    public Integer lineID;

    public TLineToken() {
        arguments = new Hashtable<String, Object>();
    }

    public TLineToken(String fullLine, Integer lineID) {
        arguments = new Hashtable<String, Object>();
        this.lineID = lineID;
        this.fullLine = fullLine;
    }
    
    public void reset() {
        arguments.clear();
    }

    public void reset(String fullLine, Integer lineID) {
        this.lineID = lineID;
        this.fullLine = fullLine;
        arguments.clear();
    }

    public void set(String what, Object toWhat) {
        try {
            if (toWhat == null) {
                arguments.put(what, false);
            } else {
                arguments.put(what, toWhat);
            }
        } catch (NullPointerException ex) {
            ErrorCollector.store_exception(ex, "TLineToken.set", false);
        }
    }

    public boolean containsKey(String what) {
        return arguments.containsKey(what);
    }

    public Object get(String what) {
        return this.get(what, null);
    }

    public Object get(String what, Object defaultResult) {
        if (!arguments.containsKey(what)) return defaultResult;
        return arguments.get(what);
    }

}

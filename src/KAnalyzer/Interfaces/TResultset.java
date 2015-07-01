/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Interfaces;

import java.util.Hashtable;

/**
 * @deprecated Replaced by TPresenter and IReportable
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class TResultset {

    public static TResultset Global;

    public Hashtable<String, Object> results;
    public Hashtable<String, String> units;

    public TResultset() {
        results = new Hashtable<String, Object>();
        units = new Hashtable<String, String>();
    }

    public void add(String group, Object value) {
        add(group, value, "");
    }

    public void add(String group, Object value, String units) {
        Object v = value;
        if (results.containsKey(group)){
            v = results.get(group);
            if (v.getClass().getName().equals("java.lang.Integer")) {
                v = (Integer)v + (Integer)value;
            } else if (v.getClass().getName().equals("java.lang.Float")) {
                v = (Float)v + (Float)value;
            } else if (v.getClass().getName().equals("java.lang.Double")) {
                v = (Double)v + (Double)value;
            } else if (v.getClass().getName().equals("java.lang.String")) {
                v = (String)v + (String)value;
            }
        } else {
            this.units.put(group, units);
        }
        results.put(group, v);
    }

    public void set(String group, Object value, String units) {
        if (results.containsKey(group)){
            results.remove(group);
            this.units.remove(group);
        }
        results.put(group, value);
        this.units.put(group, units);
    }

    public String getValue(String group) {
        if (!results.containsKey(group)) {
            return "";
        }
        return results.get(group) + " " + units.get(group).toString();
    }

}

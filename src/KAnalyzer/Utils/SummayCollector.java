/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import java.util.Hashtable;

/**
 *
 * This is a summary collector class
 * 
 * You can perform statistical operations such as stacking
 * values, counting hits, measuring averages and then
 * report the results, without messing with complex structures
 * 
 * @author Ioannis Charalampidis
 */
public class SummayCollector {

    public Hashtable<String, Hashtable<String, Object>> summaryMap;

    public SummayCollector() {
        summaryMap = new Hashtable<String, Hashtable<String, Object>>();
    }

    private Hashtable<String, Object> fetchGroup(String group) {
        if (summaryMap.containsKey(group)) {
            return summaryMap.get(group);
        } else {
            Hashtable<String, Object> groupHashtable;
            groupHashtable = new Hashtable<String, Object>();
            summaryMap.put(group, groupHashtable);
            return groupHashtable;
        }
    }

    public void setValue(String group, String key, Object value) {
        Hashtable<String, Object> grp;
        grp = fetchGroup(group);
        grp.put(key, value);
    }

    public void addValue(String group, String key, Object value) {
        Hashtable<String, Object> grp;
        grp = fetchGroup(group);
        Object obj;
        if (grp.containsKey(key)) {
            obj = grp.get(key);
            if (value.getClass() == String.class) {
                obj = (String)obj + "\n" + value.toString();
            } else if (value.getClass() == Double.class) {
                obj = (Double)obj + (Double)value;
            } else if (value.getClass() == Double.class) {
                obj = (Double)obj + (Double)value;
            } else if (value.getClass() == Long.class) {
                obj = (Long)obj + (Long)value;
            } else if (value.getClass() == Integer.class) {
                obj = (Integer)obj + (Integer)value;
            }
            grp.put(key, obj);
        } else {
            grp.put(key, value);
        }
    }

    public void substractValue(String group, String key, Object value) {
        Hashtable<String, Object> grp;
        grp = fetchGroup(group);
        Object obj;
        if (grp.containsKey(key)) {
            obj = grp.get(key);
            if (value.getClass() == Double.class) {
                obj = (Double)obj - (Double)value;
            } else if (value.getClass() == Double.class) {
                obj = (Double)obj - (Double)value;
            } else if (value.getClass() == Long.class) {
                obj = (Long)obj - (Long)value;
            } else if (value.getClass() == Integer.class) {
                obj = (Integer)obj - (Integer)value;
            }
            grp.put(key, obj);
        } else {
            grp.put(key, value);
        }
    }

    public void divideValues(String groupFrom, String keyFrom, String groupWith, String keyWith, String groupSaveAt, String keySaveAt) {
        Hashtable<String, Object> grp1, grp2, grp3;
        Object obj1, obj2, obj3;
        grp1 = fetchGroup(groupFrom);
        grp2 = fetchGroup(groupWith);
        grp3 = fetchGroup(groupSaveAt);
        obj1 = grp1.get(keyFrom);
        obj2 = grp2.get(keyWith);
        obj3 = null;

        if (obj1.getClass() == Double.class) {
            obj3 = (Double)obj1 / (Double)obj2;
        } else if (obj1.getClass() == Double.class) {
            obj3 = (Double)obj1 - (Double)obj2;
        } else if (obj1.getClass() == Long.class) {
            obj3 = (Long)obj1 - (Long)obj2;
        } else if (obj1.getClass() == Integer.class) {
            obj3 = (Integer)obj1 - (Integer)obj2;
        }

        grp3.put(keySaveAt, obj3);

    }

}

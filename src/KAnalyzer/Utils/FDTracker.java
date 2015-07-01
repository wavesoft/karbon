/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.ErrorCollector;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class FDTracker {

    public class DynamicDescriptor implements Cloneable {
        Hashtable<String, Object> map = new Hashtable<String, Object>();
        public String index = "";
        public Long fd = 0l;
        public DynamicDescriptor(String key) {
            index = key;
        }
        public DynamicDescriptor(String key, String[][] values) {
            for (String[] kv: values) {
                map.put((String)kv[0], kv[1]);
            }
            index = key;
        }
        public DynamicDescriptor(DynamicDescriptor cloneFrom, Boolean keepStats) {
            try {
                for (String key: cloneFrom.map.keySet()) {
                    if (!keepStats && (
                            "bytes_in".equals(key) ||
                            "bytes_out".equals(key) ||
                            "time".equals(key) ||
                            "failed_time".equals(key) ||
                            "hits".equals(key) ||
                            "failures".equals(key) ||
                            "worst_time".equals(key) ||
                            "worst_call".equals(key)
                       )) continue;
                    map.put(key, cloneFrom.map.get(key));
                }
            } catch (Exception ex) {
                ErrorCollector.store_exception(ex, "FDTracker.DynamicDescriptor.DynamicDescriptor(Mirror)",false);
            }
            index= cloneFrom.index;
        }
        public Object get(String key) {
            if (!map.containsKey(key)) return null;
            return map.get(key);
        }
        public Object get(String key, Object defaultValue) {
            if (!map.containsKey(key)) return defaultValue;
            return map.get(key);
        }
        public void set(String key, Object data) {
            map.put(key, data);
        }

        public Integer pileValue(String key, Integer value) {
            Integer v = (Integer)this.get(key, 0);
            v = v + value;
            map.put(key, v);
            return v;
        }
        public Double pileValue(String key, Double value) {
            Double v = (Double)this.get(key, 0d);
            v = v + value;
            map.put(key, v);
            return v;
        }
        public Long pileValue(String key, Long value) {
            Long v = (Long)this.get(key, 0l);
            v = v + value;
            map.put(key, v);
            return v;
        }
        
    }

    /*
     * The map between file descriptor and the file name description
     */
    private Hashtable<Long, DynamicDescriptor> fdmap;

    /*
     * A fast index that allows index-descriptor mapping
     */
    private Hashtable<String, DynamicDescriptor> stringIndex;

    /*
     * The archive of file descriptors
     */
    public ArrayList<DynamicDescriptor> archive;

    public FDTracker() {
        fdmap = new Hashtable<Long, DynamicDescriptor>();
        archive = new ArrayList<DynamicDescriptor>();
        stringIndex = new Hashtable<String, DynamicDescriptor>();
    }
    public FDTracker(FDTracker cloneFrom, Boolean cloneArchive) {
        fdmap = new Hashtable<Long, DynamicDescriptor>();
        archive = new ArrayList<DynamicDescriptor>();
        stringIndex = new Hashtable<String, DynamicDescriptor>();

        // Clone file descriptor map
        for (Long l: cloneFrom.fdmap.keySet()) {
            fdmap.put(l, new DynamicDescriptor(cloneFrom.fdmap.get(l),false));
        }

        // Should we also clone the archive?
        if (cloneArchive) {
            for (DynamicDescriptor dd: cloneFrom.archive) {
                archive.add(dd);
            }
            for (String str: cloneFrom.stringIndex.keySet()) {
                stringIndex.put(str, cloneFrom.stringIndex.get(str));
            }
        }

    }

    public void reset() {
        fdmap.clear();
        archive.clear();
    }

    /*
     * Archive the FD
     */
    public void archiveFD(Long fd) {
        DynamicDescriptor fdinfo = getFD(fd);
        if (fdinfo != null) archive.add(fdinfo);
    }

    /*
     * Archive all the left entries
     */
    public void archiveRemaining() {
        for (Long fd: fdmap.keySet()) {
            archive.add(fdmap.get(fd));
        }
        fdmap.clear();
    }

    /*
     * Check if we are using a file descriptor
     */
    public boolean hasFD(Long fd) {
        return fdmap.containsKey(fd);
    }

    /*
     * Release a file descriptor
     */
    public boolean releaseFD(Long fd) {
        if (!this.hasFD(fd)) return false;
        archiveFD(fd);
        fdmap.remove(fd);
        return true;
    }

    /*
     * Fetch the data of the FD
     */
    public DynamicDescriptor getFD(Long fd) {
        return fdmap.get(fd);
    }

    /*
     * Map a file description to a specified file descriptor
     */
    public DynamicDescriptor mapFD(String fdIndex, Long fd) {
        DynamicDescriptor FDesc = new DynamicDescriptor(fdIndex);
        FDesc.fd = fd;
        fdmap.put(fd, FDesc);
        stringIndex.put(FDesc.index, FDesc);
        return FDesc;
    }
    public DynamicDescriptor mapFD(DynamicDescriptor CloneFrom, Long fd) {
        DynamicDescriptor FDesc = new DynamicDescriptor(CloneFrom, false);
        String fdIndex = FDesc.index;
        
        FDesc.fd = fd;
        fdmap.put(fd, FDesc);
        stringIndex.put(FDesc.index, FDesc);
        return FDesc;
    }

    /*
     * Duplicate the file descriptor ( for dup2() system call )
     */
    public Long duplicateFD(Long fd, Long newFD) {
        mapFD(fdmap.get(fd).toString(), newFD);
        return newFD;
    }
    
    /*
     * Lookup a file descriptor either from archive or
     * from the currently open system.
     * 
     * You can specify the allocArchiveIfMissing if you want
     * to create (or not) a new entry on the archive with the
     * specified index, if it wasn't found neither in the current
     * map nor the archive.
     *
     */
    public DynamicDescriptor findDescriptor(String index, Boolean allocArchiveIfMissing) {
        DynamicDescriptor dd = null;

        // Search values
        if (stringIndex.containsKey(index)) {
            return stringIndex.get(index);
        }

        // Create new (if told so)
        if (allocArchiveIfMissing)  {
            dd = new DynamicDescriptor(index);
            stringIndex.put(index, dd);
            archive.add(dd);
        }
        return dd;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Interfaces;

import KAnalyzer.ErrorCollector;
import java.util.ArrayList;
import javax.swing.SwingWorker;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public abstract class TStream {

    private ArrayList<IStreamReceiver> receivers;

    public abstract SwingWorker getAsyncWorker();

    public TStream() {
        receivers = new ArrayList<IStreamReceiver>() {};
        receivers.clear();
    }

    public void addReceiver(IStreamReceiver receiver) {
        receivers.add(receiver);
    }

    public void removeReceiver(IStreamReceiver receiver) {
        receivers.remove(receiver);
    }
    
    public void writeLine(String line) {
        try {
            for (IStreamReceiver r : receivers) {
                if (r != null) r.receiveLine(line);
            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "TStream.writeLine", true);
        }
    }

}

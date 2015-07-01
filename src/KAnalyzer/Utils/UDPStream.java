/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.Interfaces.TStream;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class UDPStream extends TStream {

    private class DatagramTerminator implements PropertyChangeListener {

        DatagramSocket s;

        public DatagramTerminator(DatagramSocket s) {
            this.s = s;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("forced_cancel")) {
                s.close();
            }
        }

    }

    DatagramSocket sock;
    
    private static final int PROGRESS_UPDATE_THRESSHOLD = 1000;

    public UDPStream(int Port) throws SocketException {
        super();
        sock = new DatagramSocket(Port);
    }

    @Override
    public SwingWorker getAsyncWorker() {
        return new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Long timeSnap = System.nanoTime();
                Double timeDiff, prev_timeDiff = 0D;
                Integer lCount = 0;
                byte[] receiveData = new byte[1024];
                DatagramPacket p = new DatagramPacket(receiveData, receiveData.length);

                addPropertyChangeListener(new DatagramTerminator(sock));

                while (sock.isBound()) {
                    try {
                        if (sock.isClosed()) {
                            this.firePropertyChange("finished", 0,1);
                            this.firePropertyChange("error", "", "Socket was closed without a reason!");
                            return false;
                        }
                        sock.receive(p);
                        if (p == null) continue;
                        String s = new String(p.getData()).substring(0, p.getLength());
                        String[] splitArray = s.split("\\r?\\n");
                        for (int i = 0; i<splitArray.length; i++ ) {
                            if (!splitArray[i].trim().isEmpty()) {
                                writeLine(splitArray[i]);
                                lCount++;
                                if (lCount > PROGRESS_UPDATE_THRESSHOLD) {
                                    // Calculate time difference + speed
                                    timeDiff = (System.nanoTime() - timeSnap) * 0.000000001;
                                    timeSnap = System.nanoTime();
                                    timeDiff = PROGRESS_UPDATE_THRESSHOLD / timeDiff;
                                    this.firePropertyChange("speed", prev_timeDiff, timeDiff);
                                    prev_timeDiff = timeDiff;

                                    // Reset
                                    lCount = 0;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        this.firePropertyChange("finished", 0,1);
                        this.firePropertyChange("error", "",ex.getMessage());
                        return false;
                    }

                    if (this.isCancelled()) {
                        break;
                    }
                }
                
                this.firePropertyChange("finished", 0,1);
                return true;
            }
        };
    }

}

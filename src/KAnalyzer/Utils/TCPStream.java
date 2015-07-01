/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.Interfaces.TStream;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class TCPStream extends TStream {

    private class ServerShocketTerminator implements PropertyChangeListener {

        ServerSocket s;

        public ServerShocketTerminator(ServerSocket s) {
            this.s = s;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("forced_cancel")) {
                try {
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private class SocketTerminator implements PropertyChangeListener {

        Socket s;

        public SocketTerminator(Socket s) {
            this.s = s;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("forced_cancel")) {
                try {
                    s.shutdownInput();
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }

    ServerSocket sock;
    
    private static final int PROGRESS_UPDATE_THRESSHOLD = 1000;

    public TCPStream(int Port) throws IOException {
        super();
        sock = new ServerSocket(Port);
    }

    @Override
    public SwingWorker getAsyncWorker() {
        return new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                Long timeSnap = System.nanoTime();
                Double timeDiff, prev_timeDiff = 0D;
                Integer lCount = 0;

                addPropertyChangeListener(new ServerShocketTerminator(sock));

                Socket sck = sock.accept();
                if (sck == null) return false;
                InputStream ins = sck.getInputStream();
                Scanner scn = new Scanner(ins);

                addPropertyChangeListener(new SocketTerminator(sck));

                while (sck.isConnected()) {
                    if (sck.isClosed()) {
                        this.firePropertyChange("finished", 0,1);
                        return false;
                    }

                    String line = scn.nextLine();

                    writeLine(line);
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

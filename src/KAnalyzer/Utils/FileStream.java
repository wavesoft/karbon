/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.ErrorCollector;
import KAnalyzer.Interfaces.TStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class FileStream extends TStream {

    BufferedReader br;
    Long fSize;

    private static final int PROGRESS_UPDATE_THRESSHOLD = 50000;

    public FileStream(String filename) {
        super();
        try {
            File f = new File(filename);
            fSize = f.length();
            FileInputStream fstream = new FileInputStream(filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readFile() {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                writeLine(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SwingWorker getAsyncWorker() {
        SwingWorker w = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    Long tSize = 0L;
                    Integer lCount = 0;
                    Long Progress;
                    Long timeSnap = System.nanoTime();
                    Double timeDiff, prev_timeDiff = 0D;

                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line != null) {
                            writeLine(line);
                            tSize += line.length()+1;
                            lCount++;
                            if (lCount > PROGRESS_UPDATE_THRESSHOLD) {

                                // Calculate progress
                                Progress = 100 * tSize / fSize;
                                this.setProgress(Progress.intValue());

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
                        if (this.isCancelled()) {
                            break;
                        }
                    }

                    br.close();

                } catch (IOException ex) {
                    ErrorCollector.store_exception(ex, "readFileAsync", true);
                    
                    this.firePropertyChange("finished", 0,1);
                    this.firePropertyChange("error", "",ex.getMessage());
                } catch (Exception ex) {

                    this.firePropertyChange("finished", 0,1);
                    this.firePropertyChange("error", "",ex.getMessage());
                }

                this.firePropertyChange("finished", 0,1);
                return true;
            }
        };
        return w;
    }

}

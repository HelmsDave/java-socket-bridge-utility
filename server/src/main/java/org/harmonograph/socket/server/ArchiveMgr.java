
package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/**
 * Manage output to storage.
 */
public class ArchiveMgr implements Runnable {
    
    final String _connectionName;
    
    final SimpleDateFormat _dateFormat;
    
    protected final LinkedBlockingQueue<String> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;
    
    /** 
     * Simple constructor.
     * 
     * @param aConnectionName Connection Name
     */
    public ArchiveMgr(final String aConnectionName) {
        _connectionName = aConnectionName;
        _queue = new LinkedBlockingQueue<>();
        _thread = new Thread(this, "ArchiveMgr_" + aConnectionName);
        _done = false;
        
        _dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH");
        _dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
    }
    
    /** 
     * Get data queue.
     * 
     * @return Input data queue
     */
    public LinkedBlockingQueue<String> getQueue()
    {
        return _queue;
    }
    
    /** Start worker. */
    public void start()
    {
        _thread.start();
    }

    public void halt()
    {
        _done = true;
        _thread.interrupt();
    }
    

    @Override
    public void run() {

        files: while (true)
        {
            // Avoid opening file, if we never get data on socket
            while (_queue.isEmpty()) {
                Utility.pause();
            }            
            
            final String tDateString = _dateFormat.format(new Date());
            final String tFilename = "/tmp/" + tDateString + "_" + _connectionName + ".dat";
            final File tFile = new File(tFilename);
            
            final long tFreeSpaceMeg = tFile.getFreeSpace() / (1024 * 1024);
            if (tFreeSpaceMeg < 1024) {
                System.out.println(String.format(
                        "Disk out of space, %d meg free", tFreeSpaceMeg));
                Utility.pause();
                continue;
            }
  
            try (FileOutputStream tFileStream = new FileOutputStream(tFile, true);
                 OutputStreamWriter tWriter = new OutputStreamWriter(tFileStream, StandardCharsets.UTF_8);
                 BufferedWriter tBufWriter = new BufferedWriter(tWriter)) {

                messages: while (!_done) {
                    final String tLine = _queue.take();
                    if (tLine == null) {
                        System.out.println(String.format("Null line"));
                        continue;
                    }
                    if (_done)
                    {
                        continue;
                    }
                    
                    tBufWriter.write(tLine);
                    tBufWriter.newLine();
                    
                    final String tDateStringNew = _dateFormat.format(new Date());
                    if (!tDateStringNew.equals(tDateString))
                    {
                        tBufWriter.flush();
                        tBufWriter.close();
                        tWriter.close();
                        tFileStream.close();
                        continue files;
                    }
                }
            } catch (final InterruptedException ex) {
                if (!_done)
                {
                    System.out.println("Queue error: " + ex.getMessage());
                } 
            } catch (final IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
                Utility.pause();
            } 
        }
        

    }    
    
}

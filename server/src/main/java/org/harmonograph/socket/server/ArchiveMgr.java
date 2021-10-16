
package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

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

        String tDateString;
        files: while (true)
        {
            tDateString = _dateFormat.format(new Date());
            final String tFilename = "/tmp/" + tDateString + "_" + _connectionName + ".dat";
            final File tFile = new File(tFilename);
            
            try (final FileWriter tFileWriter = new FileWriter(tFile, true);
                 final BufferedWriter tBufWriter = new BufferedWriter(tFileWriter)) {

                messages: while (!_done) {
                    final String tLine = _queue.take();
                    if (tLine == null) {
                        return;
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
                        tFileWriter.close();
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
            } 
        }
        

    }    
    
}

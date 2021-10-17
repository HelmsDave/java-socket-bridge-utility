
package org.harmonograph.socket.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manage compression of data file into zip file.
 */
public class ZipManager implements Runnable {
    protected final LinkedBlockingQueue<File> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;    
    
    protected static final String kZipExtension = ".zip";
    
    protected static final int kBufferSize = 64*1024;
    
    public ZipManager()
    {
        _queue = new LinkedBlockingQueue<>();
         _thread = new Thread(this, "Zip Manager");
        _done = false;       
    }
    
    public LinkedBlockingQueue<File> getQueue() {
        return _queue;
    }    
    
    /**
     * Start worker.
     */
    public void start() {
        _thread.start();
    }

    public void halt() {
        _done = true;
        _thread.interrupt();
    }

    @Override
    public void run() {
        final byte[] tBuffer = new byte[kBufferSize];
        while (!_done) {

             final File tRawFile;
             try {
                 tRawFile = _queue.take();
             } catch (final InterruptedException ex) {
                 if (_done) {
                     return;
                 }
                 continue;
             }
             final String tZipFilename = tRawFile.getPath().replace(
                     ArchiveMgr.kRawExtension, kZipExtension);
             final File tZipFile = new File(tZipFilename);
             
            try (FileOutputStream tFileOutputStream = new FileOutputStream(tZipFile);
                 ZipOutputStream tZipOutputStream = new ZipOutputStream(tFileOutputStream);
                 FileInputStream tFileInputStream = new FileInputStream(tRawFile)) {
                
                final ZipEntry tZipEntry = new ZipEntry(tRawFile.getName());
                tZipOutputStream.putNextEntry(tZipEntry);
              
                while (!_done)
                {
                    final int tBytesRead = tFileInputStream.read(tBuffer);
                    if (tBytesRead <= 0)
                    {
                        break;
                    }
                    
                    tZipOutputStream.write(tBuffer, 0, tBytesRead);
                }
                
                tZipOutputStream.closeEntry();
               
            } catch (final IOException e) {
                System.out.println(String.format("Failed to write zip %s", tZipFile));
            }
        }
    }
}

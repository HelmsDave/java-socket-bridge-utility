
package org.harmonograph.socket.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.harmonograph.socket.util.Utility;

/**
 * Manage compression of data file into zip file.
 */
public class ArchiveMgrZip implements Runnable {
    protected final LinkedBlockingQueue<File> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;    
    
    protected final ArchiveMgrS3 _archiveMgrS3;
    
    protected static final String kZipExtension = ".zip";
    
    //protected static final int kBufferSize = 64*1024;
    
    private static final Logger kLogger
            = Logger.getLogger(ArchiveMgrZip.class.getName());     
    
    public ArchiveMgrZip(final ArchiveMgrS3 aArchiveMgrS3)
    {
        _queue = new LinkedBlockingQueue<>();
        _thread = new Thread(this, "Zip Manager");
        _thread.setPriority(Thread.MIN_PRIORITY);
        _done = false;       
        _archiveMgrS3 = aArchiveMgrS3;
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
        final byte[] tBuffer = new byte[Utility.kDiskBufferSize];
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
             
             if (tZipFile.exists())
             {
                 kLogger.info(String.format(
                         "zip exists, replacing%n%s", tZipFile.getPath()));
                 final boolean tRemoved = tZipFile.delete();
                 if (!tRemoved)
                 {
                     kLogger.info(String.format(
                         "Error, failed to remove%n%s", tZipFile.getPath()));
                 }
             }
             
             kLogger.info(String.format(
                     "Compressing %s to %s", tRawFile.getPath(), tZipFile.getPath()));
             final long tStartTimeMillis = System.currentTimeMillis();
             
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
                kLogger.info(String.format("Failed to write zip %s", tZipFile));
                continue;
            }
            
            final long tDeltaTimeMillis = System.currentTimeMillis() - tStartTimeMillis;
            kLogger.info(String.format(
                     "Done compressing %s to %s, %,dms",
                    tRawFile.getPath(), tZipFile.getPath(), tDeltaTimeMillis));
            
            // remove raw file
            final boolean tRemoved = tRawFile.delete();
            if (!tRemoved)
            {
                kLogger.info(String.format(
                    "Error, failed to remove raw%n%s", tRawFile.getPath()));
            }
            
             try {
                 _archiveMgrS3.getQueue().put(tZipFile);
             } catch (final InterruptedException ex) {
                 if (_done) {
                     return;
                 }
             }            
        }
    }
}

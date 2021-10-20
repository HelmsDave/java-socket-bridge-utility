package org.harmonograph.socket.server;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Archive logic to write to S3
 */
public class ArchiveMgrS3 implements Runnable {

    protected final LinkedBlockingQueue<File> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;

    final AmazonS3 _s3;

    protected static final String kBucketName = "serial.archive";

    private static final Logger kLogger
            = Logger.getLogger(ArchiveMgrS3.class.getName());       
    
    public ArchiveMgrS3() {
        _queue = new LinkedBlockingQueue<>();
        _thread = new Thread(this, "S3 Manager");
        _thread.setPriority(Thread.MIN_PRIORITY);
        _done = false;

        _s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1).build();
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

        while (!_done) {

            final File tZipFile;
            try {
                tZipFile = _queue.take();
            } catch (final InterruptedException ex) {
                if (_done) {
                    return;
                }
                continue;
            }

            kLogger.info(String.format(
                    "Pushing object to S3, %s to %s", tZipFile.getPath(), kBucketName));
            final long tStartTimeMillis = System.currentTimeMillis();
            try {
                _s3.putObject(kBucketName, tZipFile.getName(), tZipFile);

            } catch (final AmazonServiceException tEx) {
                kLogger.info(String.format(
                        "Failed to push to s3%n%s", tEx.getMessage()));
                continue;
            }

            // remove zip file
            final boolean tRemoved = tZipFile.delete();
            if (!tRemoved) {
                kLogger.info(String.format(
                        "Error, failed to remove raw%n%s", tZipFile.getPath()));
            }

            final long tDeltaTimeMillis = System.currentTimeMillis() - tStartTimeMillis;
            kLogger.info(String.format(
                    "Done Pushing object to S3, %s to %s, %,dms",
                    tZipFile.getPath(), kBucketName, tDeltaTimeMillis));
        }
    }
}

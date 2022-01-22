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
import java.util.logging.Logger;
import org.harmonograph.socket.util.Utility;

/**
 * Manage output to storage.
 */
public class ArchiveMgr implements Runnable, DistributionMgrClient {

    protected final String _connectionName;

    protected final SimpleDateFormat _dateFormat;

    protected long _lastFlush;

    protected final LinkedBlockingQueue<String> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;
    
    protected final ArchiveMgrZip _zipMgr;

    protected static final long kFlushPeriodSeconds = 10;
    
    
    
    protected static final String kArchivePath = "/tmp";
    
    protected static final String kDateFormat = "yyyy_MM_dd_HH";
    
    protected static final String kRawExtension = ".dat";

    private static final Logger kLogger
            = Logger.getLogger(ArchiveMgr.class.getName());       
    
    /**
     * Simple constructor.
     *
     * @param aConnectionName Connection Name
     */
    public ArchiveMgr(final String aConnectionName, final ArchiveMgrZip aZipMgr) {
        _connectionName = aConnectionName;
        _queue = new LinkedBlockingQueue<>();
        _thread = new Thread(this, "Archive Manager");
        _done = false;
        _zipMgr = aZipMgr;

        _dateFormat = new SimpleDateFormat(kDateFormat);
        _dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String getConnectionName() {
        return "Archive Manager";
    }

    @Override
    public boolean isConnected() {
        return _thread.isAlive();
    }

    /**
     * Get data queue.
     *
     * @return Input data queue
     */
    @Override
    public LinkedBlockingQueue<String> getQueue() {
        return _queue;
    }

    /**
     * Start worker.
     */
    @Override
    public void start() {
        _thread.start();
    }

    @Override
    public void halt() {
        _done = true;
        _thread.interrupt();
    }

    @Override
    public void run() {
        try {
            
            // Check for orphan files
            final String tCurrentDateString = _dateFormat.format(new Date());
            final int tExpectedFilenameLength
                    = kDateFormat.length() + 1 + _connectionName.length() + kRawExtension.length();
            for (final File tFile : new File(kArchivePath).listFiles())
            {
                final String tFilename = tFile.getName();
                if (!tFile.isFile() ||  !tFile.canRead()
                        || !tFilename.endsWith(kRawExtension)
                        || !tFilename.contains(_connectionName)
                        || (tFilename.length() !=  tExpectedFilenameLength)
                        || tFilename.startsWith(tCurrentDateString))
                {
                    continue;
                }
                kLogger.info(String.format(
                            "likely orphan %s, continuing archive", tFile.getName()));
                try {
                    _zipMgr.getQueue().put(tFile);
                } catch (final InterruptedException ex) {
                    if (_done) {
                        return;
                    }
                    continue;
                }
            }
            
            // Loop to name and write output files
            files:
            while (!_done) {

                // Avoid opening file, if we never get data on socket
                String tPendingLine;
                try {
                    tPendingLine = _queue.take();
                } catch (final InterruptedException ex) {
                    if (_done) {
                        return;
                    }
                    continue;
                }

                // Figure filename an open file for write
                final String tDateString = _dateFormat.format(new Date());
                final String tFilename = kArchivePath + "/" + tDateString + "_" + _connectionName + kRawExtension;
                final File tFile = new File(tFilename);
                kLogger.info(String.format(
                        "Open file for write (exists=%b)%n%s", tFile.exists(), tFile.getPath()));

                try (FileOutputStream tFileStream = new FileOutputStream(tFile, true);
                        OutputStreamWriter tWriter = new OutputStreamWriter(tFileStream, StandardCharsets.UTF_8);
                        BufferedWriter tBufWriter = new BufferedWriter(tWriter, Utility.kDiskBufferSize)) {

                    _lastFlush = System.currentTimeMillis();

                    // Write messages to file
                    messages:
                    while (!_done) {
                        try {

                            if (tPendingLine == null) {
                                kLogger.info(String.format("Null line"));
                                continue;
                            }
                            if (_done) {
                                tBufWriter.flush();
                                return;
                            }

                            // Check free space before write
                            final long tFreeSpaceMeg = tFile.getFreeSpace() / (1024 * 1024);
                            if (tFreeSpaceMeg < 1024) {
                                kLogger.info(String.format(
                                        "Disk out of space, %d meg free", tFreeSpaceMeg));
                                try {
                                    Thread.sleep(Utility.kSleepTimeMillis);
                                } catch (final InterruptedException ex2) {
                                    if (_done) {
                                        return;
                                    }
                                }
                                continue;
                            }

                            // Write proper
                            tBufWriter.write(tPendingLine);
                            tBufWriter.newLine();

                            // Time=based flush
                            final long tCurrentTime = System.currentTimeMillis();
                            if ((tCurrentTime - _lastFlush) > kFlushPeriodSeconds) {
                                tBufWriter.flush();
                                _lastFlush = tCurrentTime;
                            }

                            // Check if it's time to open another file
                            final String tDateStringNew = _dateFormat.format(new Date());
                            if (!tDateStringNew.equals(tDateString)) {
                                kLogger.info(String.format("Close file%n%s", tFile.getPath()));
                                tBufWriter.flush();
                                tBufWriter.close();
                                tWriter.close();
                                tFileStream.close();
                                _zipMgr.getQueue().put(tFile);
                                continue files;
                            }
                        } finally {
                            tPendingLine = _queue.take();
                        }
                    }
                } catch (final InterruptedException ex) {
                    if (_done) {
                        return;
                    }
                } catch (final IOException ex) {
                    kLogger.info("I/O error: " + ex.getMessage());
                    try {
                        Thread.sleep(Utility.kSleepTimeMillis);
                    } catch (final InterruptedException ex2) {
                        if (_done) {
                            return;
                        }
                    }
                }
            }

        } finally {
            kLogger.info(String.format("Archive thread exit done=%b", _done));
        }
    }

}

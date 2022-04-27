package org.harmonograph.socket.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.harmonograph.socket.util.Utility;

/**
 * Message distribution manager.
 */
public class DistributionMgr implements Runnable {

    protected final short _port;
    protected final boolean _verbose;
    protected final int _bufferSize;
    
    protected final LinkedBlockingQueue<String> _queue;

    protected final List<DistributionMgrClient> _downlinks;

    protected final Thread _threadQueueWorker;

    protected long _reportTimeLast;
    protected int _reportLines;
    protected int _reportChars;
    protected volatile boolean _done;

    private static final Logger kLogger
            = Logger.getLogger(DistributionMgr.class.getName());     
    
    /**
     * Simple constructor.
     * @param aPort Downlink port number
     * @param aVerbose Verbose control
     * @param aBufferSize Buffer size in chars
     * @param aQueue Main message queue
     */
    public DistributionMgr(
            final short aPort,
            final boolean aVerbose,
            final int aBufferSize,
            final LinkedBlockingQueue<String> aQueue) {
        _port = aPort;
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        _queue = aQueue;
        _downlinks = new CopyOnWriteArrayList<>();
        _done = false;

        _threadQueueWorker = new Thread(this, "DistributionMgr");
    }

    public void addListener(final DistributionMgrClient aClient)
    {
        _downlinks.add(aClient);
        aClient.start();
    }

    public void start() {
        _threadQueueWorker.start();
    }

    public void halt()
    {
        _done = true;
        _threadQueueWorker.interrupt();
        for (final DistributionMgrClient tDown : _downlinks) {
            tDown.halt();
        }
    }    
    public String getStatus()
    {
        final StringBuilder tStatus = new StringBuilder();
        tStatus.append(String.format("Distribution Consumers: %,d%n", _downlinks.size())); 
        return tStatus.toString();
    }  
    
        @Override
        public void run() {
            while (!_done) {
                try {
                    final String tLine = _queue.take();
                    if (tLine == null) {
                        try {
                           Thread.sleep(Utility.kSleepTimeMillis);
                        } catch (final InterruptedException ex2) {
                            if (_done)
                            {
                                return;
                            }                   
                        } 
                        continue;
                    }
                    
                    final List<DistributionMgrClient> tToRemove = new ArrayList<>();
                    for (final DistributionMgrClient tDown : _downlinks) {
                        if (_done)
                        {
                            return;
                        }
                        if (!tDown.isConnected()) {
                            kLogger.info(String.format(
                                    "Removing client %s",
                                    tDown.getConnectionName()));                               
                            tDown.halt();
                            tToRemove.add(tDown);
                            continue;
                        }

                        tDown.getQueue().put(tLine);
                    }
                    if (!tToRemove.isEmpty())
                    {
                        _downlinks.removeAll(tToRemove);
                    }
                    
                    if (_verbose) {
                        ++_reportLines;
                        _reportChars += tLine.length();
                        final long tDeltaTimeMillis
                                = System.currentTimeMillis() - _reportTimeLast;
                        if (tDeltaTimeMillis > Utility.kLogTimeMillis) {
                            kLogger.info(String.format(
                                    "Received %,.1f lines/sec, %,.1fk chars/sec, %d listeners%n%s",
                                    _reportLines / (tDeltaTimeMillis / 1000f),
                                    (_reportChars / 1024f) / (tDeltaTimeMillis / 1000f),
                                    _downlinks.size(),
                                    tLine));
                            _reportLines = 0;
                            _reportChars = 0;
                            _reportTimeLast = System.currentTimeMillis();
                        }
                    }
                } catch (final InterruptedException tEx) {
                    if (_done)
                    {
                        return;
                    }                   
                }
            }
        }

}

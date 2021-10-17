package org.harmonograph.socket.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
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
                    for (final DistributionMgrClient tDown : _downlinks) {
                        if (_done)
                        {
                            return;
                        }
                        if (!tDown.isConnected()) {
                            _downlinks.remove(tDown);
                            continue;
                        }
                        
                        final int tBacklog = tDown.getQueue().size();
                        if (tBacklog > Utility.kBacklogMessagesWarning)
                        {
                            System.out.println(String.format(
                                    "Backlog on client %s, %d messages", 
                                    tDown.getConnectionName(), tBacklog));
                        }
                        if (tBacklog > Utility.kBacklogMessagesMax)
                        {
                            System.out.println(String.format(
                                    "Removing client %s",
                                    tDown.getConnectionName()));                            
                            tDown.halt();
                            _downlinks.remove(tDown);
                            continue;
                        }

                        tDown.getQueue().put(tLine);
                    }
                    
                    if (_verbose) {
                        ++_reportLines;
                        _reportChars += tLine.length();
                        if (System.currentTimeMillis() - _reportTimeLast > Utility.kLogTimeMillis) {
                            System.out.print(String.format(
                                    "Received %,d lines, %,dk chars, %d listeners%n",
                                    _reportLines, _reportChars / 1024, _downlinks.size()));
                            _reportLines = 0;
                            _reportChars = 0;
                            System.out.println(tLine);
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

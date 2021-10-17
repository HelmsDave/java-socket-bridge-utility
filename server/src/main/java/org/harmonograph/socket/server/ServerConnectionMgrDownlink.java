package org.harmonograph.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/**
 * Connection manager for down-link server, creates client connections.
 */
public class ServerConnectionMgrDownlink {

    protected final short _port;
    protected final boolean _verbose;
    protected final int _bufferSize;
    
    protected final LinkedBlockingQueue<String> _queue;

    protected final List<ClientConnectionMgrDownlink> _downlinks;
    
    protected final ArchiveMgr _archiveMgr;

    protected final Thread _threadQueueWorker;
    protected final Thread _threadServerListener;

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
     * @param aArchiveMgr Archive Manager
     */
    public ServerConnectionMgrDownlink(
            final short aPort,
            final boolean aVerbose,
            final int aBufferSize,
            final LinkedBlockingQueue<String> aQueue,
            final ArchiveMgr aArchiveMgr) {
        _port = aPort;
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        _queue = aQueue;
        _downlinks = new CopyOnWriteArrayList<>();
        _archiveMgr = aArchiveMgr;

        _threadQueueWorker = new Thread(new QueueWorker(), "Queue worker");
        _threadServerListener = new Thread(new ServerListener(), "Server Listener");
    }

    /** Serve connections on server socket. */
    public void serve() {
        try (final ServerSocket tPullServerSocket = new ServerSocket(_port)) {

            final Socket tClient = tPullServerSocket.accept();
            tClient.getInetAddress().getCanonicalHostName();
            final String tConnectionName
                    = tClient.getInetAddress().getCanonicalHostName() + "_"
                    + tClient.getPort();

            final ClientConnectionMgrDownlink tHandler
                    = new ClientConnectionMgrDownlink(
                            tClient, _verbose, _bufferSize, tConnectionName);
            _downlinks.add(tHandler);
            tHandler.start();
        } catch (final Exception tEx) {

        }
    }

    public void start() {
        _threadQueueWorker.start();
        _threadServerListener.start();
    }

    public void halt()
    {
        _done = true;
        _threadQueueWorker.interrupt();
        _threadServerListener.interrupt();
    }    
    
    /** 
     * Thread to listen to main queue for messages and distribute
     * a copy to each download client.
     */
    class QueueWorker implements Runnable {

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
                    for (final ClientConnectionMgrDownlink tDown : _downlinks) {
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
                    
                    _archiveMgr.getQueue().put(tLine);
                    
                    if (_verbose) {
                        ++_reportLines;
                        _reportChars += tLine.length();
                        if (System.currentTimeMillis() - _reportTimeLast > Utility.kSleepTimeMillis) {
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

    /** Thread to listen for client connections. */
    class ServerListener implements Runnable {

        @Override
        public void run() {
            while (true) {
                serve();
                try {
                   Thread.sleep(Utility.kSleepTimeMillis);
                } catch (final InterruptedException ex2) {
                    if (_done)
                    {
                        return;
                    }                   
                } 
            }
        }
    }

}

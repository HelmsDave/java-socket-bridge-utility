package org.harmonograph.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.harmonograph.socket.util.Utility;

/**
 * Connection manager for down-link server, creates client connections.
 */
public class ServerConnectionMgrDownlink implements Runnable {

    protected final short _port;
    protected final boolean _verbose;
    protected final int _bufferSize;
    
    protected final LinkedBlockingQueue<String> _queue;

    protected final DistributionMgr _distributionMgr;  

    protected final Thread _threadServerListener;

    protected volatile boolean _done;

    protected int _connectionCount;
    
    private static final Logger kLogger
            = Logger.getLogger(ServerConnectionMgrDownlink.class.getName());     
    
    /**
     * Simple constructor.
     * @param aPort Downlink port number
     * @param aVerbose Verbose control
     * @param aBufferSize Buffer size in chars
     * @param aQueue Main message queue
     * @param aDistributionMgr Distribution manager
     */
    public ServerConnectionMgrDownlink(
            final short aPort,
            final boolean aVerbose,
            final int aBufferSize,
            final LinkedBlockingQueue<String> aQueue,
            final DistributionMgr aDistributionMgr) {
        _port = aPort;
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        _queue = aQueue;
        _done = false;
        _distributionMgr = aDistributionMgr;
        _connectionCount = 0;

        _threadServerListener = new Thread(this, "Server Listener Downlink");
    }

    /** Serve connections on server socket. */
    public void serve() {
        try (final ServerSocket tPullServerSocket = new ServerSocket(_port)) {

            final Socket tClient = tPullServerSocket.accept();
            tClient.getInetAddress().getCanonicalHostName();
            final String tConnectionName
                    = tClient.getInetAddress().getCanonicalHostName() + "_"
                    + tClient.getPort();
            ++_connectionCount;
            kLogger.info(String.format(
                    "Downlink new connection %s (%d)", tConnectionName, _connectionCount));
            final ClientConnectionMgrDownlink tHandler
                    = new ClientConnectionMgrDownlink(
                            tClient, _verbose, _bufferSize, tConnectionName);
            _distributionMgr.addListener(tHandler);
            tHandler.start();
        } catch (final Exception tEx) {

        }
    }

    public void start() {
        _threadServerListener.start();
    }

    public void halt()
    {
        _done = true;
        _threadServerListener.interrupt();
    }    
  
    public String getStatus()
    {
        final StringBuilder tStatus = new StringBuilder();
        tStatus.append(String.format("Downlink Connection Count: %,d%n", _connectionCount)); 
        return tStatus.toString();
    }      
    
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

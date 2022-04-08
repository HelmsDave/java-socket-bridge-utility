package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.harmonograph.socket.util.Utility;

/** Connection manager for single client connection, down-link mode. */
public class ClientConnectionMgrDownlink 
        implements Runnable, DistributionMgrClient {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;  
    protected final boolean _verbose;
    protected final int _bufferSize;
    protected boolean _connected;
    protected final String _connectionName;
    protected final Thread _thread;
    protected volatile boolean _done;
    protected int _messagesProcessed;

    private static final Logger kLogger
            = Logger.getLogger(ClientConnectionMgrDownlink.class.getName());      
    
    /**
     * Simple constructor.
     * @param aSocket Client socket
     * @param aVerbose Verbose control
     * @param aBufferSize Buffer size in chars
     * @param aConnectionName Name of this socket connection
     */
    public ClientConnectionMgrDownlink(
            final Socket aSocket,
            final boolean aVerbose,
            final int aBufferSize,
            final String aConnectionName) {
        
        _socket = aSocket;
        _queue = new LinkedBlockingQueue<>();
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        _connected = true;
        _connectionName = aConnectionName;
        _thread = new Thread(this, "Downlink_" + aConnectionName);
        _done = false;
        _messagesProcessed = 0;
    }
    
    /** Start worker. */
    @Override
    public void start()
    {
        _thread.start();
    }
    
    @Override
    public void halt()
    {
        _done = true;
        _thread.interrupt();
    }
    
    /** 
     * Get human readable connection name.
     * 
     * @return Connection name 
     */
    @Override
    public String getConnectionName()
    {
        return _connectionName;
    }
    
    @Override
    public LinkedBlockingQueue<String> getQueue()
    {
        return _queue;
    }
    
    @Override
    public boolean isConnected()
    {
        if (!_connected)
        {
            return false;
        }
        
        final int tBacklog = _queue.size();
        if (tBacklog > Utility.kBacklogMessagesWarning)
        {
            kLogger.info(String.format(
                    "Backlog on client %s, %d processed, %d messages", 
                    _connectionName, _messagesProcessed, tBacklog));
        }
        if (tBacklog > Utility.kBacklogMessagesMax)
        {                         
            return false;
        }
        
        return true;
    }

    @Override
    public void run() {

        try (final OutputStream tOutputStream = _socket.getOutputStream();
                final OutputStreamWriter tWriter = new OutputStreamWriter(tOutputStream);
                final BufferedWriter tBufWriter = new BufferedWriter(tWriter, _bufferSize)) {

            System.out.print(String.format("Downlink Connected from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));
            while (!_done) {
                final String tLine = _queue.take();
                if (tLine == null) {
                    _connected = false;
                    return;
                }
                if (!_socket.isConnected())
                {
                    _connected = false;
                    return;
                }
                if (tLine.isEmpty())
                {
                    continue;
                }

                if (_done)
                {
                    return;
                }
                tBufWriter.write(tLine);
                tBufWriter.newLine();
                ++_messagesProcessed;
            }
        } catch (InterruptedException ex) {
            if (!_done)
            {
                kLogger.info("Queue error: " + ex.getMessage());
            } 
        } catch (IOException ex) {
            System.out.print(String.format("Lost downlink connection from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));
        }
        _connected = false;
    }
}

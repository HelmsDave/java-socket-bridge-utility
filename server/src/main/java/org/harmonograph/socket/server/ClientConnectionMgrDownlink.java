package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

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
        _thread = new Thread(this, aConnectionName);
        _done = false;
        
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
    
    /** 
     * Get human readable connection name.
     * 
     * @return Connection name 
     */
    public String getConnectionName()
    {
        return _connectionName;
    }
    
    
    public LinkedBlockingQueue<String> getQueue()
    {
        return _queue;
    }
    
    public boolean isConnected()
    {
        return _connected;
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
                if (_done)
                {
                    return;
                }
                tBufWriter.write(tLine);
                tBufWriter.newLine();
            }
        } catch (InterruptedException ex) {
            if (!_done)
            {
                System.out.println("Queue error: " + ex.getMessage());
            } 
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
        _connected = false;
    }
}

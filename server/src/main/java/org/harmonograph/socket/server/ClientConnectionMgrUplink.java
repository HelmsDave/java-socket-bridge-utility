package org.harmonograph.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/** Connection manager for single client connection, uplink mode. */
public class ClientConnectionMgrUplink implements Runnable {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;
    protected final boolean _verbose;
    protected final int _bufferSize;
    protected final String _connectionName;
    protected final Thread _thread;
    protected volatile boolean _done;

    /**
     * Simple constructor.
     * @param aSocket Client socket
     * @param aQueue Message queue
     * @param aVerbose Verbose control
     * @param aBufferSize Buffer size in chars
     * @param aConnectionName Name of this socket connection
     */
    public ClientConnectionMgrUplink(
            final Socket aSocket,
            final LinkedBlockingQueue<String> aQueue,
            final boolean aVerbose,
            final int aBufferSize,
            final String aConnectionName) {
        _socket = aSocket;
        _queue = aQueue;
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        _connectionName = aConnectionName;
        _thread = new Thread(this, aConnectionName);
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
    
    @Override
    public void run() {

        try (final InputStream tInputStream = _socket.getInputStream();
                final InputStreamReader tReader = new InputStreamReader(tInputStream);
                final BufferedReader tBufReader = new BufferedReader(tReader, _bufferSize)) {

            System.out.print(String.format(
                    "Uplink Connected from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));            
            while (!_done) {
                
                final String tLine = tBufReader.readLine();
                if (tLine == null) {
                    System.out.print(String.format(
                            "Pull Connection lost from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));            
                    return;
                }
                _queue.put(tLine);
            }
            
        } catch (InterruptedException ex) {
            System.out.println("Queue error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

    }
}

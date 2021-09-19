package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/** Connection manager for single client connection, downlink mode. */
public class ClientConnectionMgrDownlink implements Runnable {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;
    protected final boolean _verbose;
    protected boolean _connected;

    public ClientConnectionMgrDownlink(
            final Socket aSocket,
            final boolean aVerbose) {
        _socket = aSocket;
        _queue = new LinkedBlockingQueue<>();
        _verbose = aVerbose;
        _connected = true;
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
                final BufferedWriter tBufWriter = new BufferedWriter(tWriter, Utility.kBufferSize)) {

            System.out.print(String.format("Downlink Connected from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));
            while (true) {
                final String tLine = _queue.take();
                if (tLine == null) {
                    _connected = false;
                    return;
                }
                tBufWriter.write(tLine);
                tBufWriter.newLine();
            }
        } catch (InterruptedException ex) {
            System.out.println("Queue error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
        _connected = false;
    }
}

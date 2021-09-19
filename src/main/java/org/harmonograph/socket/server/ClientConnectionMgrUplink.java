package org.harmonograph.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/** Connection manager for single client connection, uplink mode. */
public class ClientConnectionMgrUplink implements Runnable {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;
    protected final boolean _verbose;

    public ClientConnectionMgrUplink(
            final Socket aSocket,
            final LinkedBlockingQueue<String> aQueue,
            final boolean aVerbose) {
        _socket = aSocket;
        _queue = aQueue;
        _verbose = aVerbose;
    }

    @Override
    public void run() {

        try (final InputStream tInputStream = _socket.getInputStream();
                final InputStreamReader tReader = new InputStreamReader(tInputStream);
                final BufferedReader tBufReader = new BufferedReader(tReader, Utility.kBufferSize)) {

            System.out.print(String.format(
                    "Uplink Connected from %s %d%n",
                    _socket.getInetAddress().getCanonicalHostName(),
                    _socket.getPort()));            
            while (true) {
                
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

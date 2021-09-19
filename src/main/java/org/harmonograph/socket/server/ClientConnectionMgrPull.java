package org.harmonograph.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/** Connection manager for single client connection, pull mode. */
public class ClientConnectionMgrPull implements Runnable {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;
    protected final boolean _verbose;

    public ClientConnectionMgrPull(
            final Socket aSocket,
            final LinkedBlockingQueue<String> aQueue,
            final boolean aVerbose) {
        _socket = aSocket;
        _queue = aQueue;
        _verbose = aVerbose;
    }

    @Override
    public void run() {

        try (final InputStream tPullInputStream = _socket.getInputStream();
                final InputStreamReader tPullReader = new InputStreamReader(tPullInputStream);
                final BufferedReader tPullBufReader = new BufferedReader(tPullReader, Utility.kBufferSize)) {

            while (true) {
                
                final String tLine = tPullBufReader.readLine();
                if (tLine == null) {
                    System.out.print(String.format("Connection lost%n"));
                    return;
                }
                _queue.put(tLine);

                if (_verbose) {
                    System.out.println(tLine);
                }
            }

        } catch (InterruptedException ex) {
            System.out.println("Queue error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

    }
}

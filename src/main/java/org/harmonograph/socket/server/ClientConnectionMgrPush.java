package org.harmonograph.socket.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/** Connection manager for single client connection, push mode. */
public class ClientConnectionMgrPush implements Runnable {

    protected final Socket _socket;
    protected final LinkedBlockingQueue<String> _queue;
    protected final boolean _verbose;

    public ClientConnectionMgrPush(
            final Socket aSocket,
            final LinkedBlockingQueue<String> aQueue,
            final boolean aVerbose) {
        _socket = aSocket;
        _queue = aQueue;
        _verbose = aVerbose;
    }

    @Override
    public void run() {

        try (final OutputStream tOutputStream = _socket.getOutputStream();
                final OutputStreamWriter tWriter = new OutputStreamWriter(tOutputStream);
                final BufferedWriter tBufWriter = new BufferedWriter(tWriter)) {

            System.out.print(String.format("Connected%n"));
            while (true) {
                final String tLine = _queue.take();
                if (tLine == null) {
                    return;
                }
                tBufWriter.append(tLine);
                tBufWriter.newLine();
            }
        } catch (InterruptedException ex) {
            System.out.println("Queue error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}

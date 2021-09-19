package org.harmonograph.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/**
 * Connection manager for single server connection, uplink.
 */
public class ServerConnectionMgrUplink {

    protected final short _port;
    protected final boolean _verbose;

    protected final LinkedBlockingQueue<String> _queue;

    protected final Thread _threadServerListener;

    public ServerConnectionMgrUplink(
            final short aPort,
            final boolean aVerbose,
            final LinkedBlockingQueue<String> aQueue) {
        _port = aPort;
        _verbose = aVerbose;
        _queue = aQueue;

        _threadServerListener = new Thread(new ServerListener(), "Server Listener");
    }

    public void serve() {
        try (final ServerSocket tPullServerSocket = new ServerSocket(_port)) {

            final Socket tClient = tPullServerSocket.accept();
            tClient.getInetAddress().getCanonicalHostName();
            final String tConnectionName
                    = tClient.getInetAddress().getCanonicalHostName() + "_"
                    + tClient.getPort();

            final ClientConnectionMgrUplink tHandler
                    = new ClientConnectionMgrUplink(tClient, _queue, _verbose);

            final Thread tServiceThread = new Thread(tHandler, tConnectionName);
            tServiceThread.start();
        } catch (final Exception tEx) {

        }
    }

    public void start() {
        _threadServerListener.start();
    }

    class ServerListener implements Runnable {

        public void run() {
            while (true) {
                serve();
                Utility.pause();
            }
        }
    }

}

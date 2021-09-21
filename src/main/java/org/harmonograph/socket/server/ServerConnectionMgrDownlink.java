package org.harmonograph.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;

/**
 * Connection manager for single server connection, downlink.
 */
public class ServerConnectionMgrDownlink {

    protected final short _port;
    protected final boolean _verbose;

    protected final LinkedBlockingQueue<String> _queue;

    protected final List<ClientConnectionMgrDownlink> _downlinks;

    protected final Thread _threadQueueWorker;
    protected final Thread _threadServerListener;

    protected long _reportTimeLast;
    protected int _reportLines;
    protected int _reportChars;

    public ServerConnectionMgrDownlink(
            final short aPort,
            final boolean aVerbose,
            final LinkedBlockingQueue<String> aQueue) {
        _port = aPort;
        _verbose = aVerbose;
        _queue = aQueue;
        _downlinks = new CopyOnWriteArrayList<>();

        _threadQueueWorker = new Thread(new QueueWorker(), "Queue worker");
        _threadServerListener = new Thread(new ServerListener(), "Server Listener");
    }

    public void serve() {
        try (final ServerSocket tPullServerSocket = new ServerSocket(_port)) {

            final Socket tClient = tPullServerSocket.accept();
            tClient.getInetAddress().getCanonicalHostName();
            final String tConnectionName
                    = tClient.getInetAddress().getCanonicalHostName() + "_"
                    + tClient.getPort();

            final ClientConnectionMgrDownlink tHandler
                    = new ClientConnectionMgrDownlink(tClient, _verbose);
            _downlinks.add(tHandler);

            final Thread tServiceThread = new Thread(tHandler, tConnectionName);
            tServiceThread.start();
        } catch (final Exception tEx) {

        }
    }

    public void start() {
        _threadQueueWorker.start();
        _threadServerListener.start();
    }

    class QueueWorker implements Runnable {

        public void run() {
            while (true) {
                try {
                    final String tLine = _queue.take();
                    if (tLine == null) {
                        Utility.pause();
                        return;
                    }
                    for (final ClientConnectionMgrDownlink tDown : _downlinks) {
                        if (!tDown.isConnected()) {
                            _downlinks.remove(tDown);
                            continue;
                        }

                        tDown.getQueue().put(tLine);
                    }
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
                    Utility.pause();
                }
            }
        }
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


package org.harmonograph.socket.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import org.harmonograph.socket.util.Utility;


/** 
 * Connection manager for single server connection.
 * Any one server is either pull (receiving) or push (serving) messages.
 */
public class ServerConnectionMgr implements Runnable {
    
    protected final short _port;
    protected final boolean _verbose;
    protected final boolean _pullMode;
    
    protected final LinkedBlockingQueue<String> _queue;    
    
    public ServerConnectionMgr(
            final short aPort,
            final boolean aVerbose,
            final LinkedBlockingQueue<String> aQueue,
            final boolean aPullMode)
    {
        _port = aPort;
        _verbose = aVerbose;
        _queue = aQueue;
        _pullMode = aPullMode;
    }

    public void run()
    {
        while (true)
        {
            serve();
            Utility.pause();
        }
        
    }
    public void serve()
    {
        try(final ServerSocket tPullServerSocket = new ServerSocket(_port)) {
            
            final Socket tClient = tPullServerSocket.accept();
            tClient.getInetAddress().getCanonicalHostName();
            final String tConnectionName
                    = tClient.getInetAddress().getCanonicalHostName() + "_"
                    + tClient.getPort();
            
            final Runnable tHandler;
            if (_pullMode)
            {
                tHandler = new ClientConnectionMgrPull(tClient, _queue, _verbose);
            } else {
                 tHandler = new ClientConnectionMgrPush(tClient, _queue, _verbose);
            }
                
            final Thread tServiceThread = new Thread(tHandler, tConnectionName);
            tServiceThread.start();  
        }
        catch (final Exception tEx)
        {
            
        }
    }
}

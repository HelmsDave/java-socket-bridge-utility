
package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 */
public class SockServerRelay {
    
    protected final ServerConnectionMgr _pullServer;
    protected final ServerConnectionMgr _pushServer;
    
    protected final Thread _pullServerThread;
    protected final Thread _pushServerThread;
    
    protected final LinkedBlockingQueue<String> _queue;    
    
    public SockServerRelay(
            final short aPullPort,
            final short aPushPort,
            final boolean aVerbose)
    {
        _queue = new LinkedBlockingQueue<>();
        _pullServer = new ServerConnectionMgr(
                aPullPort, aVerbose, _queue, true);
        _pullServerThread = new Thread(_pullServer, "Pull Server");
        
        
        _pushServer = new ServerConnectionMgr(
                aPushPort, aVerbose, _queue, false);
        _pushServerThread = new Thread(_pushServer, "Push Server");
        
        System.out.print(String.format(
                "SockServerRelay, pull %d, push %d, verbose %b%n",
                aPullPort, aPushPort, aVerbose));        
    }

    public void start()
    {
        _pullServerThread.start();
        _pushServerThread.start();
    }
  
}

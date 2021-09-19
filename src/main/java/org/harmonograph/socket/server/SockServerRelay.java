
package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 */
public class SockServerRelay {
    
    protected final ServerConnectionMgr _uplinkServer;
    protected final ServerConnectionMgr _downlinkServer;
    
    protected final Thread _uplinkServerThread;
    protected final Thread _downlinkServerThread;
    
    protected final LinkedBlockingQueue<String> _queue;    
    
    public SockServerRelay(
            final short aUplinkPort,
            final short aDownlinkPort,
            final boolean aVerbose)
    {
        _queue = new LinkedBlockingQueue<>();
        _uplinkServer = new ServerConnectionMgr(
                aUplinkPort, aVerbose, _queue, true);
        _uplinkServerThread = new Thread(_uplinkServer, "Uplink Server");
        
        
        _downlinkServer = new ServerConnectionMgr(
                aDownlinkPort, aVerbose, _queue, false);
        _downlinkServerThread = new Thread(_downlinkServer, "Downlink Server");
        
        System.out.print(String.format(
                "SockServerRelay, uplink %d, downlink %d, verbose %b%n",
                aUplinkPort, aDownlinkPort, aVerbose));        
    }

    public void start()
    {
        _uplinkServerThread.start();
        _downlinkServerThread.start();
    }
  
}

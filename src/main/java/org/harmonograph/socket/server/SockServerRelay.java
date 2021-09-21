
package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 */
public class SockServerRelay {
    
    protected final ServerConnectionMgrUplink _uplinkServer;
    protected final ServerConnectionMgrDownlink _downlinkServer;
    
    protected final LinkedBlockingQueue<String> _queue;    
    
    public SockServerRelay(
            final short aUplinkPort,
            final short aDownlinkPort,
            final boolean aVerbose,
            final int aBufferSize)
    {
        _queue = new LinkedBlockingQueue<>();
        _uplinkServer = new ServerConnectionMgrUplink(
                aUplinkPort, aVerbose, aBufferSize, _queue);
        
        _downlinkServer = new ServerConnectionMgrDownlink(
                aDownlinkPort, aVerbose, aBufferSize, _queue);
        
        System.out.print(String.format(
                "SockServerRelay, uplink %d, downlink %d, verbose %b%n",
                aUplinkPort, aDownlinkPort, aVerbose));        
    }

    public void start()
    {
        _uplinkServer.start();
        _downlinkServer.start();
    }
  
}


package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 */
public class SockServerRelay {
    /** Server for up-link connections. */
    protected final ServerConnectionMgrUplink _uplinkServer;
    /** Server for down-link connections. */
    protected final ServerConnectionMgrDownlink _downlinkServer;
    /** Queue of messages between up-link and down-link, merged. */
    protected final LinkedBlockingQueue<String> _queue;    
    
    /**
     * Simple constructor.
     * @param aUplinkPort Uplink server port
     * @param aDownlinkPort Downlink server port
     * @param aVerbose Verbose control
     * @param aBufferSize Bufer size in chars
     */
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

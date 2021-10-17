
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
    /** Message distribution manager. */
    protected final DistributionMgr _distributionMgr;
    
    /** Zip output manager. */
    protected final ZipManager _zipManager;
    
    /** Data archive manager. */
    protected final ArchiveMgr _archiveMgr;
    
    
    /** Queue of messages between up-link and down-link, merged. */
    protected final LinkedBlockingQueue<String> _queue;    
    
    /**
     * Simple constructor.
     * @param aUplinkPort Uplink server port
     * @param aDownlinkPort Downlink server port
     * @param aVerbose Verbose control
     * @param aBufferSize Buffer size in chars
     * @param aName Connection Name
     * @param aArchive Enable archive
     */
    public SockServerRelay(
            final short aUplinkPort,
            final short aDownlinkPort,
            final boolean aVerbose,
            final int aBufferSize,
            final String aName,
            final boolean aArchive)
    {
        _queue = new LinkedBlockingQueue<>();
        _uplinkServer = new ServerConnectionMgrUplink(
                aUplinkPort, aVerbose, aBufferSize, _queue);
        
        _distributionMgr = new DistributionMgr(
                aDownlinkPort, aVerbose, aBufferSize, _queue);
        
        if (aArchive)
        {
            _zipManager = new ZipManager();
            _zipManager.start();
            _archiveMgr = new ArchiveMgr(aName, _zipManager);
            _distributionMgr.addListener(_archiveMgr);
        }
        else
        {
            _zipManager = null;
            _archiveMgr = null;
        }

        _downlinkServer = new ServerConnectionMgrDownlink(
                aDownlinkPort, aVerbose, aBufferSize, _queue, _distributionMgr);
        
        System.out.print(String.format(
                "SockServerRelay, uplink %d, downlink %d, verbose %b%n",
                aUplinkPort, aDownlinkPort, aVerbose));        
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> halt(), "shutdown_hook"));
    }

    public void start()
    {
        _uplinkServer.start();
        _downlinkServer.start();
        _distributionMgr.start();
    }
    
    public void halt()
    {
        System.out.print(String.format("Halting server"));
        _uplinkServer.halt();
        _downlinkServer.halt();
        _distributionMgr.halt();
    }    
  
}

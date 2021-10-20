
package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

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
    
    /** Data archive manager. */
    protected final ArchiveMgr _archiveMgr;
    /** Zip output manager. */
    protected final ArchiveMgrZip _archiveMgrZip;
    /** S3 manager. */
    protected final ArchiveMgrS3 _archiveMgrS3;
    
    
    /** Queue of messages between up-link and down-link, merged. */
    protected final LinkedBlockingQueue<String> _queue;    
    
    private static final Logger kLogger
            = Logger.getLogger(SockServerRelay.class.getName());    
    
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
            _archiveMgrS3 = new ArchiveMgrS3();
            _archiveMgrZip = new ArchiveMgrZip(_archiveMgrS3);
            _archiveMgr = new ArchiveMgr(aName, _archiveMgrZip);
            _distributionMgr.addListener(_archiveMgr);
            
        }
        else
        {
            _archiveMgrS3 = null;
            _archiveMgrZip = null;
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
        if (_archiveMgrZip != null)
        {
            _archiveMgrZip.start();
        }
        if (_archiveMgrS3 != null)
        {
            _archiveMgrS3.start();
        }
    }
    
    public void halt()
    {
        System.out.print(String.format("Halting server"));
        System.out.flush();
        _uplinkServer.halt();
        _downlinkServer.halt();
        _distributionMgr.halt();
        _archiveMgrZip.halt();
        _archiveMgrS3.halt();
    }    
  
}

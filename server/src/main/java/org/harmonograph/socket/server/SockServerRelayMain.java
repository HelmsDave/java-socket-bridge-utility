
package org.harmonograph.socket.server;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 * Supports multiple push connections and multiple pull connections.
 */
public final class SockServerRelayMain {
   
    private static final Logger kLogger
            = Logger.getLogger(SockServerRelayMain.class.getName());
    
    /**
     * @param aArgs the command line arguments
     */
    public static void main(final String[] aArgs) throws NumberFormatException {
        short tUplinkPort = 30004;
        short tDownlinkPort = 30003;
        boolean tVerbose = false;
        int tBufferSize = 4*1024;
        String tName = "";
        boolean tArchive = false;

        for (final Handler tHandler : Logger.getLogger("").getHandlers())
        {
            if (tHandler instanceof ConsoleHandler)
            {
                tHandler.setFormatter(new LocalFormatter());
            }
        }
        
        for (int tIndex = 0; tIndex < aArgs.length; ++tIndex)
        {
            switch (aArgs[tIndex])
            {
                case "-uplinkPort":
                    final String tUplinkPortString = aArgs[++tIndex];
                    tUplinkPort = Short.parseShort(tUplinkPortString);
                    break;
                case "-downlinkPort":
                    final String tDownlinkPortString = aArgs[++tIndex];
                    tDownlinkPort = Short.parseShort(tDownlinkPortString);
                    break;
                case "-verbose":
                    tVerbose = true;
                    break;
                case "-bufferSize":
                    final String tBufferSizeString = aArgs[++tIndex];
                    tBufferSize = Integer.parseInt(tBufferSizeString);
                    break;
                case "-name":
                    tName = aArgs[++tIndex];
                    break;
                case "-archive":
                    tArchive = true;
                    break;                    
                default:
                    kLogger.info(String.format(
                            "unknown arg %s", aArgs[tIndex]));
                    break;                    
            }
        }
        
        final SockServerRelay tServer
                = new SockServerRelay(tUplinkPort, tDownlinkPort,
                        tVerbose, tBufferSize, tName, tArchive);
        tServer.start();
        while (true)
        {
            try
            {
               Thread.sleep(10L * 60L * 1000L);
            } catch (final InterruptedException tEx)
            {
            }
            
            final String tStatus = tServer.getStatus();
            kLogger.info(tStatus);
        }
    }
    
static class LocalFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append("[");
        if ((record.getSourceClassName() != null) && !record.getSourceClassName().isEmpty())
        {
            final String[] tSplit = record.getSourceClassName().split("\\.");
            builder.append(tSplit[tSplit.length - 1]);
        }
        builder.append(".");
        if ((record.getSourceMethodName() != null) && !record.getSourceMethodName().isEmpty())
        {
            builder.append(record.getSourceMethodName());
        }
        builder.append(" ");
        builder.append(record.getLevel());
        builder.append("] ");
        builder.append(formatMessage(record));
        builder.append(String.format("%n"));
        return builder.toString();
    }
}    
}

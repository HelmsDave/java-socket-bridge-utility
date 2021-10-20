
package org.harmonograph.socket.bridge;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Relay application for socket clients.
 * This pulls from one server and pushes to another.
 */
public final class SockClientRelayMain {
    
    private static final Logger kLogger
            = Logger.getLogger(SockClientRelayMain.class.getName());       
    
    private SockClientRelayMain()
    {
    }
    
    /**
     * @param aArgs the command line arguments
     */
    public static void main(final String[] aArgs) throws NumberFormatException {
        String tPullHost = "localhost";
        short tPullPort = 30003;
        String tPushHost = "";
        short tPushPort = 30004;
        boolean tVerbose = false;
        int tBufferSize = 4*1024;
        
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
                case "-pullHost":
                    tPullHost = aArgs[++tIndex];
                    break;
                case "-pullPort":
                    final String tPullPortString = aArgs[++tIndex];
                    tPullPort = Short.parseShort(tPullPortString);
                    break;
                case "-pushHost":
                    tPushHost = aArgs[++tIndex];
                    break;
                case "-pushPort":
                    final String tPushPortString = aArgs[++tIndex];
                    tPushPort = Short.parseShort(tPushPortString);
                    break;
                case "-verbose":
                    tVerbose = true;
                    break;
                case "-bufferSize":
                    final String tBufferSizeString = aArgs[++tIndex];
                    tBufferSize = Integer.parseInt(tBufferSizeString);
                    break;
                default:
                    kLogger.info(String.format(
                            "unknown arg %s", aArgs[tIndex]));
                    break;
            }
        }
        final SockClientRelay tApp = new SockClientRelay(
                tPullHost, tPullPort, tPushHost, tPushPort,
                tVerbose, tBufferSize);
        tApp.run();        
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

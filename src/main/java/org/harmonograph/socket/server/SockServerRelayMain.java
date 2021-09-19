
package org.harmonograph.socket.server;


/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 */
public final class SockServerRelayMain {
   
    /**
     * @param aArgs the command line arguments
     */
    public static void main(final String[] aArgs) throws NumberFormatException {
        short tPullPort = 30003;
        short tPushPort = 30004;
        boolean tVerbose = false;

        for (int tIndex = 0; tIndex < aArgs.length - 1; ++tIndex)
        {
            switch (aArgs[tIndex])
            {
                case "-pullPort":
                    final String tPullPortString = aArgs[++tIndex];
                    tPullPort = Short.parseShort(tPullPortString);
                    break;
                case "-pushPort":
                    final String tPushPortString = aArgs[++tIndex];
                    tPushPort = Short.parseShort(tPushPortString);
                    break;
                case "-verbose":
                    tVerbose = true;
                    break;
            }
        }
        
        final SockServerRelay tServer
                = new SockServerRelay(tPullPort, tPushPort, tVerbose);
        tServer.start();
    }
    
}

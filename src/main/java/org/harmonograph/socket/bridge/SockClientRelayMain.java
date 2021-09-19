
package org.harmonograph.socket.bridge;


/**
 * Relay application for socket clients.
 * This pulls from one server and pushes to another.
 */
public final class SockClientRelayMain {
    
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
        
        for (int tIndex = 0; tIndex < aArgs.length - 1; ++tIndex)
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
            }
        }
        final SockClientRelay tApp = new SockClientRelay(
                tPullHost, tPullPort, tPushHost, tPushPort, tVerbose);
        tApp.run();        
    }
    
}

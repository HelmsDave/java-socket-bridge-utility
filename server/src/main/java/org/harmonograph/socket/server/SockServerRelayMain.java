
package org.harmonograph.socket.server;


/**
 * Socket server relay application.
 * This pulls from port and pushes to another.
 * Supports multiple push connections and multiple pull connections.
 */
public final class SockServerRelayMain {
   
    /**
     * @param aArgs the command line arguments
     */
    public static void main(final String[] aArgs) throws NumberFormatException {
        short tUplinkPort = 30004;
        short tDownlinkPort = 30003;
        boolean tVerbose = false;
        int tBufferSize = 4*1024;
        String tName = "";

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
                default:
                    System.out.println(String.format(
                            "unknown arg %s", aArgs[tIndex]));
                    break;                    
            }
        }
        
        final SockServerRelay tServer
                = new SockServerRelay(tUplinkPort, tDownlinkPort,
                        tVerbose, tBufferSize, tName);
        tServer.start();
    }
    
}
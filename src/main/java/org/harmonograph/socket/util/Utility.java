
package org.harmonograph.socket.util;

/** Simple thread pause, used all over. */
public final class Utility {
      
    /** Simple thread pause. */
    public static void pause()
    {
        try {
            Thread.sleep(10*1000);
        } catch (final InterruptedException tEx) {   
        }   
    }
    
}

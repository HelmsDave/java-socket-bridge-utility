
package org.harmonograph.socket.util;

/** Simple thread pause, used all over. */
public final class Utility {
      
    /** General sleep time in milliseconds. */
    public static final int kSleepTimeMillis = 10*1000;
    
    /** Simple thread pause. */
    public static void pause()
    {
        try {
            Thread.sleep(kSleepTimeMillis);
        } catch (final InterruptedException tEx) {   
        }   
    }
    
}

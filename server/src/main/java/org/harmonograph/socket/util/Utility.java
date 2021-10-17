
package org.harmonograph.socket.util;

/** Simple thread pause, used all over. */
public final class Utility {
    
    /** Warning threshold for backlog messages. */
    public static final int kBacklogMessagesWarning = 100;
    /** Max backlog messages. */
    public static final int kBacklogMessagesMax = 100;      
    
    
    /** General sleep time in milliseconds. */
    public static final int kSleepTimeMillis = 10*1000;
    
    /** General log period in milliseconds. */
    public static final int kLogTimeMillis = 60*1000;    
    
}

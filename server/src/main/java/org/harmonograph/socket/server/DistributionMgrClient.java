
package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client interface for message from distribution manager.
 */
public interface DistributionMgrClient {
    
    LinkedBlockingQueue<String> getQueue();
    
    boolean isConnected();
    
    String getConnectionName();
    
    void start();
     
    void halt();
}

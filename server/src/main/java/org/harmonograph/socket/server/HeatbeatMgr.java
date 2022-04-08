package org.harmonograph.socket.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/** Connection manager for single client connection, uplink mode. */
public class HeatbeatMgr implements Runnable {

    protected final LinkedBlockingQueue<String> _queue;
    protected final Thread _thread;
    protected volatile boolean _done;

    private static final Logger kLogger
            = Logger.getLogger(HeatbeatMgr.class.getName());     
    
    /**
     * Simple constructor.
     * @param aQueue Message queue
     */
    public HeatbeatMgr(
            final LinkedBlockingQueue<String> aQueue) {
        _queue = aQueue;
        _thread = new Thread(this, "Heartbeat");
        _done = false;
    }
    
    /** Start worker. */
    public void start()
    {
        _thread.start();
    }

    
    public void halt()
    {
        _done = true;
        _thread.interrupt();
    }           
    
    @Override
    public void run(){

        while (!_done) {
            try {
                Thread.sleep(1000);
                _queue.put("");
            } catch (InterruptedException ex) {
                kLogger.info("Queue error: " + ex.getMessage());
            }                
            
        }
    }
}

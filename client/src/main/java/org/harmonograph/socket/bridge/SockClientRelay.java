
package org.harmonograph.socket.bridge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.logging.Logger;
import org.harmonograph.socket.util.Utility;


/**
 * Relay application for socket clients.
 * This pulls from one server and pushes to another.
 */
public class SockClientRelay implements Runnable {
    
    protected final String _pullHost;
    protected final short _pullPort;
    protected final String _pushHost;
    protected final short _pushPort;
    protected final boolean _verbose;
    protected final int _bufferSize;
    
    protected long _reportTimeLast;
    protected int _reportLines;
    protected int _reportChars; 
    
    private static final Logger kLogger
            = Logger.getLogger(SockClientRelay.class.getName());     
    
    public SockClientRelay(
            final String aPullHost, final short aPullPort,
            final String aPushHost, final short aPushPort,
            final boolean aVerbose, final int aBufferSize)
    {
        _pullHost = aPullHost;
        _pullPort = aPullPort;
        _pushHost = aPushHost;
        _pushPort = aPushPort;
        _verbose = aVerbose;
        _bufferSize = aBufferSize;
        
        System.out.print(String.format(
                "SockClientRelay, pull %s %d, push %s %d, verbose %b, buffer %d%n",
                _pullHost, _pullPort, _pushHost, _pushPort, _verbose, _bufferSize));
    }

    @Override
    public void run()
    {
        while (true)
        {
            connect();
            Utility.pause();
        }
        
    }
    
    /** 
     * This method tries to make a socket connection to both the source data
     * socket server (pull) and the destination data socket server (push).
     * If the connection is made, then this class will read messages from the
     * source (lines) and push them to the destination.
     */
    public void connect()
    {
        System.out.print(String.format("Connecting to pull server %s:%d%n", _pullHost, _pullPort));
        try (final Socket tPullSock = new Socket(_pullHost, _pullPort);
             final InputStream tPullInputStream = tPullSock.getInputStream();
             final InputStreamReader tPullReader = new InputStreamReader(tPullInputStream);
             final BufferedReader tPullBufReader = new BufferedReader(tPullReader, _bufferSize)) {

            System.out.print(String.format("Connecting to push server %s:%d%n", _pushHost, _pushPort));
            try (final Socket tPushSock = new Socket(_pushHost, _pushPort);
                 final OutputStream tPushOutputStream = tPushSock.getOutputStream();
                 final OutputStreamWriter tPushWriter = new OutputStreamWriter(tPushOutputStream);
                 final BufferedWriter tPushBufWriter = new BufferedWriter(tPushWriter, _bufferSize)) {
                
                System.out.print(String.format("Connected%n"));
                while (true) {

                    final String tLine = tPullBufReader.readLine();                
                    
                    if (tLine == null) {
                        System.out.print(String.format("Connection lost%n"));
                        return;
                    }
                    
                    if (_verbose)
                    {
                        ++_reportLines;
                        _reportChars += tLine.length();
                        if (System.currentTimeMillis() - _reportTimeLast > Utility.kSleepTimeMillis)
                        {
                            System.out.print(String.format(
                                    "Sent %,d lines, %,dk chars%n",
                                    _reportLines, _reportChars/1024));
                            _reportLines = 0;
                            _reportChars = 0;
                            kLogger.info(tLine);
                            _reportTimeLast = System.currentTimeMillis();
                        }                        
                    }                       
                    
                    tPushBufWriter.write(tLine);
                    tPushBufWriter.newLine();
                }
            } catch (UnknownHostException ex) {
                kLogger.info("Push Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                kLogger.info("I/O error: " + ex.getMessage());
            }
        } catch (UnknownHostException ex) {
            kLogger.info("Pull Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            kLogger.info("I/O error: " + ex.getMessage());
        }
    }
    
}

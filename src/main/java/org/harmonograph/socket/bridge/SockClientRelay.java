
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
    
    public SockClientRelay(
            final String aPullHost, final short aPullPort,
            final String aPushHost, final short aPushPort,
            final boolean aVerbose)
    {
        _pullHost = aPullHost;
        _pullPort = aPullPort;
        _pushHost = aPushHost;
        _pushPort = aPushPort;
        _verbose = aVerbose;
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
    public void connect()
    {
        System.out.print(String.format("Connecting to pull server %s:%d%n", _pullHost, _pullPort));
        try (final Socket tPullSock = new Socket(_pullHost, _pullPort);
             final InputStream tPullInputStream = tPullSock.getInputStream();
             final InputStreamReader tPullReader = new InputStreamReader(tPullInputStream);
             final BufferedReader tPullBufReader = new BufferedReader(tPullReader, Utility.kBufferSize)) {

            System.out.print(String.format("Connecting to push server %s:%d%n", _pushHost, _pushPort));
            try (final Socket tPushSock = new Socket(_pushHost, _pushPort);
                 final OutputStream tPushOutputStream = tPushSock.getOutputStream();
                 final OutputStreamWriter tPushWriter = new OutputStreamWriter(tPushOutputStream);
                 final BufferedWriter tPushBufWriter = new BufferedWriter(tPushWriter, Utility.kBufferSize)) {
                
                System.out.print(String.format("Connected%n"));
                final char[] tBuffer = new char[Utility.kBufferSize];
                while (true) {
                    //final int tRead = tPullBufReader.read(tBuffer);
                    //if (tRead < 0)
                    //{
                    //    break;
                    //}
                    //tPushBufWriter.write(tBuffer, 0, tRead);
                    
                    //if (_verbose && (tRead > 40))
                    //{
                    //    final String tSnippet = new String(tBuffer, 0, 40);
                    //    System.out.println(tSnippet);
                    //}
                    
                    final String tLine = tPullBufReader.readLine();
                    if (tLine == null) {
                        System.out.print(String.format("Connection lost%n"));
                        return;
                    }
                    tPushBufWriter.write(tLine);
                    tPushBufWriter.newLine();
                    if (_verbose)
                    {
                        System.out.println(tLine);
                    }
                }
            } catch (UnknownHostException ex) {
                System.out.println("Push Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        } catch (UnknownHostException ex) {
            System.out.println("Pull Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
    
}

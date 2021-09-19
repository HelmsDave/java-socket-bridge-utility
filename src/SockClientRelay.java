
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.UnknownHostException;


/**
 * Relay application for socket clients.
 * This pulls from one server and pushes to another.
 */
public class SockClientRelay {
    
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

    public void run()
    {
        while (true)
        {
            connect();
            pause();
        }
        
    }
    public void connect()
    {
        System.out.print(String.format("Connecting to pull server %s:%d%n", _pullHost, _pullPort));
        try (final Socket tPullSock = new Socket(_pullHost, _pullPort);
             final InputStream tPullInputStream = tPullSock.getInputStream();
             final InputStreamReader tPullReader = new InputStreamReader(tPullInputStream);
             final BufferedReader tPullBufReader = new BufferedReader(tPullReader)) {

            System.out.print(String.format("Connecting to push server %s:%d%n", _pushHost, _pushPort));
            try (final Socket tPushSock = new Socket(_pushHost, _pushPort);
                 final OutputStream tPushOutputStream = tPushSock.getOutputStream();
                 final OutputStreamWriter tPushWriter = new OutputStreamWriter(tPushOutputStream);
                 final BufferedWriter tPushBufWriter = new BufferedWriter(tPushWriter)) {
                
                System.out.print(String.format("Connected%n"));
                while (true) {
                    final String tLine = tPullBufReader.readLine();
                    if (tLine == null) {
                        System.out.print(String.format("Connection lost%n"));
                        return;
                    }
                    tPushBufWriter.append(tLine);
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
    
    protected void pause()
    {
        try {
            Thread.sleep(10*1000);
        } catch (final InterruptedException tEx) {   
        }   
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

package RoC.NetworkProtocolsBench;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class TCPServer implements BaseServer {
    private int m_nPort= 0;
    private boolean m_bShutdown = false;
    private long m_nBeginTime= 0;
    private boolean m_bBeginIsSet = false;
    private List<Long> m_aTimes;

    @Override
    public void SetPort(int nPort)
    {
        m_nPort = nPort;
    }

    @Override
    public void ListentoPort(WorkerRunnable.LatencyWriter oWriter)
    {
        ServerSocket oServerSocket = null;
        try {
            int nI = 0;

            System.out.println("Listen to port TCP...");
            oServerSocket = new ServerSocket(m_nPort + nI);

            nI++;
            Socket oConnectedSocket = oServerSocket.accept();
            System.out.println("Conected");
            long nBeginTime = 0;
            long nLatency = 0;
            long nBenchID = 0;

            while (!m_bShutdown) {
                DataInputStream oMsgClient = new DataInputStream(oConnectedSocket.getInputStream());

                int nLen = oMsgClient.readInt();
                nLatency = BenchNetworkTime.GetCurrentTime() - nBeginTime;
                if(nLen > 0) {
                    byte[] message = new byte[nLen];
                    oMsgClient.readFully(message, 0 , nLen);
                    if (nLen > 16) {
                   /* System.out.println("-------------");
                    System.out.println("String to 16:"+ sBuf.substring(0,8));
                    System.out.println("String to id:"+ sBuf.substring(8,8));*/
                        nBeginTime = 0;
                        nBenchID = 0;
                        for (int i = 0; i < 8; i++) {
                            nBeginTime <<= 8;
                            nBenchID <<= 8;
                            nBeginTime |= (message[i] & 0xFF);
                            nBenchID |= (message[i+8] & 0xFF);
                        }
                    System.out.println("#########");
                    System.out.println("nBenchID: " + nBenchID);
                    System.out.println("#########");

                    oWriter.WriteResultstoFile("TCPP", nBenchID, (BenchNetworkTime.GetCurrentTime() - nBeginTime), nLatency);

                    }
                }
            }
            oServerSocket.close();
            System.out.println("Closing");
        } catch (IOException e) {

            try {
                oServerSocket.close();
            } catch (IOException ex) {

            }
        }
    }

    @Override
    public boolean NeedLoop() {
        return true;
    }



}

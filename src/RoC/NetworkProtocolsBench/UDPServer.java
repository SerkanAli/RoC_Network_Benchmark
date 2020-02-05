package RoC.NetworkProtocolsBench;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPServer implements BaseServer
{
    private int m_nPort;
    private long m_nBeginTime = 0;
    private boolean m_bBeginIsSet = false;
    @Override
    public void SetPort(int nPort) {m_nPort = nPort;}

    @Override
    public void ListentoPort(WorkerRunnable.LatencyWriter oWriter) {
        try {
            System.out.println("Listen to port UDP...");
            DatagramSocket oDataSocket = null;

                oDataSocket = new DatagramSocket(m_nPort);

            byte[] receive = new byte[65535];

            DatagramPacket DpReceive = null;
            DpReceive = new DatagramPacket(receive, receive.length);
            boolean bFirstMsg = true;
            long nBeginTime =0;
            long nLatency = 0;
            long nBenchID =0;
            long nPackets = 1;

            while (nPackets > 0)
            {
                //System.out.println("Waiting...");
                oDataSocket.receive(DpReceive);
                if(bFirstMsg)
                {
                     nBeginTime = 0;
                     nBenchID = 0;
                     nPackets = 0;
                     for(int i = 0; i < 8; i++)
                     {
                        nBeginTime <<= 8;
                        nBenchID <<= 8;
                        nPackets <<= 8;
                        nBeginTime |= (receive[i] &0xFF);
                        nBenchID |= (receive[i+8] &0xFF);
                        nPackets |= (receive[i+16] &0xFF);
                     }
                     nLatency = BenchNetworkTime.GetCurrentTime() - nBeginTime;
                     bFirstMsg = false;
                }
                nPackets--;
                receive = new byte[65535];
            }
            System.out.println("#########");
            System.out.println("nBenchID: " + nBenchID);
            System.out.println("#########");

            oWriter.WriteResultstoFile("TCPP", nBenchID, (BenchNetworkTime.GetCurrentTime() - nBeginTime), nLatency);

            oDataSocket.disconnect();
            oDataSocket.close();
            System.out.println("Closing");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean NeedLoop() {
        return true;
    }


    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (i < 3)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    } }

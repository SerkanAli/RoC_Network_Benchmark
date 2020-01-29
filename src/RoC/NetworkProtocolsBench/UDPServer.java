package RoC.NetworkProtocolsBench;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements BaseServer
{
    private int m_nPort;
    private long m_nBeginTime = 0;
    private boolean m_bBeginIsSet = false;
    @Override
    public void SetPort(int nPort) {m_nPort = nPort;}

    @Override
    public void ListentoPort() throws IOException{

        System.out.println("Listen to port UDP...");
        DatagramSocket oDataSocket = new DatagramSocket(m_nPort);
        byte[] receive = new byte[65535];

        DatagramPacket DpReceive = null;
        DpReceive = new DatagramPacket(receive, receive.length);

        while (true)
        {


            System.out.println("Waiting...");
            oDataSocket.receive(DpReceive);
           /* if(!m_bBeginIsSet)
            {
                m_nBeginTime = BenchNetworkTime.GetCurrentTime();
                m_bBeginIsSet = true;
            }*/

            System.out.println("Client:-" + data(receive));
            receive = new byte[65535];

            if(!oDataSocket.isClosed())
                break;
        }
        oDataSocket.disconnect();
        oDataSocket.close();
        System.out.println("Closing");
    }

    @Override
    public void ShutDownServer(){}



    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (i < 4)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    } }

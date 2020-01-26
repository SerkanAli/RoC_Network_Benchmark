package RoC.NetworkProtocolsBench;

import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class UDPClient implements BaseClient
{
    private int m_nPort = 0;
    private String m_sIPAdress="";

    private DatagramSocket m_odataSocket;
    private InetAddress m_oIP;

    private boolean m_bIsConnected = false;

    @Override
    public void SetPort(int nPort){m_nPort = nPort;}

    @Override
    public void SetIPAdress(String sIP){m_sIPAdress = sIP;}

    @Override
    public String SendStringCreateNewConnection(String sData) throws IOException
    {

        // Step 1:Create the socket object for
        // carrying the data.
        DatagramSocket ds = new DatagramSocket();
        InetAddress ip = InetAddress.getByName(m_sIPAdress);
        byte buf[] = null;
        String inp = sData;

        // convert the String input into the byte array.
        buf = inp.getBytes();

        // Step 2 : Create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend =
                new DatagramPacket(buf, buf.length, ip, m_nPort);

        // Step 3 : invoke the send call to actually send
        // the data.
        ds.send(DpSend);

        return "";
    }

    @Override
    public void CreateConnection() {
        try {
            m_odataSocket = new DatagramSocket();
            m_oIP = InetAddress.getByName(m_sIPAdress);
            m_bIsConnected = true;
        } catch (SocketException | UnknownHostException e) {
            m_bIsConnected = false;
        }
    }

    final int nChunkSize = 65500;
    @Benchmark
    @Override
    public String SendStringOverConnection(String sData) throws IOException {
        if(!m_bIsConnected)
            return null;
        byte aData[] = null;
        String inp = sData;
        aData = inp.getBytes();
        int nCount = (aData.length / nChunkSize) +1;
        for(int nIndex = 0; nIndex < nCount; nIndex++)
        {
            int nBegin = nIndex * nChunkSize;
            byte buf[] = Arrays.copyOfRange(aData, nBegin, nBegin+nChunkSize);
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, m_oIP, m_nPort);
            m_odataSocket.send(DpSend);
        }
        return "";
    }

    @Override
    public void CloseConnection() {
        m_odataSocket.disconnect();
        m_bIsConnected = false;
    }

    @Override
    public long GetServerBeginTime() {
        return 0;
    }
}

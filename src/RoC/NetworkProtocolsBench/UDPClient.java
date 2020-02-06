package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
    public void SendStringCreateNewConnection(String sData) throws IOException
    {
        DatagramSocket ds = new DatagramSocket();
        InetAddress ip = InetAddress.getByName(m_sIPAdress);
        byte buf[] = null;
        String inp = sData;


        buf = inp.getBytes();


        DatagramPacket DpSend =
                new DatagramPacket(buf, buf.length, ip, m_nPort);

        ds.send(DpSend);
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

    @Override
    public boolean SendStringOverConnection(Float nFileSize, BenchDataSet oData, long nBenchID) throws IOException {
        if(!m_bIsConnected)
            return false;
        int nCount = (oData.data.get(nFileSize).length / nChunkSize) +1;
        byte[] bTime = ByteBuffer.allocate(Long.BYTES).putLong(BenchNetworkTime.GetCurrentTime()).array();
        byte[] bID = ByteBuffer.allocate(Long.BYTES).putLong(nBenchID).array();
        byte[] bLenght = ByteBuffer.allocate(Long.BYTES).putLong(nCount).array();
        for(int nIndex = 0; nIndex < 8; nIndex++) {
            oData.data.get(nFileSize)[nIndex] = bTime[nIndex];
            oData.data.get(nFileSize)[nIndex + 8] = bID[nIndex];
            oData.data.get(nFileSize)[nIndex + 16] = bLenght[nIndex];
        }

        for(int nIndex = 0; nIndex < nCount; nIndex++)
        {
            int nBegin = nIndex * nChunkSize;
            byte buf[] = Arrays.copyOfRange(oData.data.get(nFileSize), nBegin, nBegin+nChunkSize);
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, m_oIP, m_nPort);
            m_odataSocket.send(DpSend);
        }
        return true;
    }

    @Override
    public void CloseConnection() {
        if(m_odataSocket != null) {
            m_odataSocket.close();
            m_odataSocket.disconnect();
        }
        m_bIsConnected = false;
    }

    @Override
    public String GetProtocolName() {
        return "UDP";
    }

    @Override
    public long GetServerBeginTime() {
        return 0;
    }
}

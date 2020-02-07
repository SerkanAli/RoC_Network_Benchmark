package RoC.NetworkProtocolsBench;

import javafx.scene.effect.InnerShadow;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TCPClient implements BaseClient {
    private int m_nPort =0;
    private String m_sIPadress ="";

    private Socket m_oSocket;
    private DataOutputStream m_oOutToServer;

    private boolean m_bIsConnected = false;


    @Override
    public void SetPort(int nPort) {
        m_nPort = nPort;
    }

    @Override
    public void SetIPAdress(String sIP) {
        m_sIPadress = sIP;
    }

    @Override
    public void SendStringCreateNewConnection(String sData) throws IOException
    {
        Socket clientSocket = new Socket(m_sIPadress, m_nPort);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes(sData + '\n');

        clientSocket.close();
    }

    @Override
    public void CreateConnection() {
       try {
            m_oSocket = new Socket(m_sIPadress, m_nPort);
            m_oOutToServer = new DataOutputStream(m_oSocket.getOutputStream());
            m_bIsConnected = true;
        } catch (IOException e) {
            m_bIsConnected = false;
        }
    }
    final int nChunkSize = 65500;
    @Override
    public boolean SendStringOverConnection(Float nFileSize, BenchDataSet oData, long nBenchID) throws IOException {

        if(!m_bIsConnected)
            return false;
        try {

            byte[] bTime = ByteBuffer.allocate(Long.BYTES).putLong(BenchNetworkTime.GetCurrentTime()).array();
            byte[] bID = ByteBuffer.allocate(Long.BYTES).putLong(nBenchID).array();
            for(int nIndex = 0; nIndex < 8; nIndex++) {
                oData.data.get(nFileSize)[nIndex] = bTime[nIndex];
                oData.data.get(nFileSize)[nIndex + 8] = bID[nIndex];
            }
            int nCount = (oData.data.get(nFileSize).length / nChunkSize) +1;
            for(int nIndex = 0; nIndex < nCount; nIndex++)
            {
                int nBegin = nIndex * nChunkSize;
                byte buf[] = Arrays.copyOfRange(oData.data.get(nFileSize), nBegin, nBegin+nChunkSize);
                //oData.data.get(nFileSize)[oData.data.get(nFileSize).length-1] = (byte)'\n';
                m_oOutToServer.writeInt(buf.length);
                m_oOutToServer.write(buf);
            }

            //m_oOutToServer.writeBytes(String.valueOf(BenchNetworkTime.GetCurrentTime())+String.valueOf(nBenchID)  + oData.data.get(nFileSize));
           // m_oOutToServer.write(oData.data.get(nFileSize));
        }catch( SocketException e){
            return false;
        }
        return true;
    }

    @Override
    public void CloseConnection() {
        try {
            if(m_oSocket != null)
                m_oSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_bIsConnected = false;
    }

    @Override
    public String GetProtocolName() {
        return "TCP";
    }

    @Override
    public long GetServerBeginTime() {
        return 0;
    }
}

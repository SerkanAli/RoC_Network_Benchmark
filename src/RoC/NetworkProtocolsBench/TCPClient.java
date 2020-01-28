package RoC.NetworkProtocolsBench;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

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
    public String SendStringCreateNewConnection(String sData) throws IOException
    {
        Socket clientSocket = new Socket(m_sIPadress, m_nPort);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes(sData + '\n');

        clientSocket.close();
        return "";
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

    @Benchmark
    @Override
    public String SendStringOverConnection(String sData) throws IOException {

        if(!m_bIsConnected)
            return null;

        m_oOutToServer.writeBytes(sData + '\n');

        return "";
    }

    @Override
    public void CloseConnection() {
        try {
            m_oSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_bIsConnected = false;
    }

    @Override
    public long GetServerBeginTime() {
        return 0;
    }
}

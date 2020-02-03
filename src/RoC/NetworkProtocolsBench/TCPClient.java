package RoC.NetworkProtocolsBench;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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

    @Override
    public void SendStringOverConnection(String sData) throws IOException {

        if(!m_bIsConnected)
            return;
        try {
            m_oOutToServer.writeBytes(sData + '\n');
        }catch( SocketException e){

        }
        return;
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

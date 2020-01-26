package RoC.NetworkProtocolsBench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements BaseServer {
    int m_nPort= 0;
    boolean m_bShutdown = false;
    long m_nBeginTime= 0;
    boolean m_bBeginIsSet = false;

    @Override
    public void SetPort(int nPort)
    {
        m_nPort = nPort;
    }

    @Override
    public void ListentoPort() throws IOException
    {
        String clientSentence;
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(m_nPort);


        while (!m_bShutdown)
        {
            System.out.println("Listen to port...");
            Socket connectionSocket = welcomeSocket.accept();
            /*if(!m_bBeginIsSet)
            {
                m_nBeginTime = BenchNetworkTime.GetCurrentTime();
                m_bBeginIsSet = true;
            }*/
            BufferedReader inFromClient =new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            System.out.println(inFromClient.readLine());
        }
    }

    @Override
    public void ShutDownServer()
    {
        m_bShutdown = true;
    }

    @Override
    public long GetBeginTime() {
        m_bBeginIsSet = false;
        return m_nBeginTime;
    }
}

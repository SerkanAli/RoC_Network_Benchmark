package RoC.NetworkProtocolsBench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements BaseServer {
    int m_nPort= 0;
    boolean m_bShutdown = false;
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
            BufferedReader inFromClient =new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            //DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientSentence = inFromClient.readLine();

            //if(clientSentence == null)
            //  continue;
            //capitalizedSentence = clientSentence.toUpperCase() + '\n';
            //outToClient.writeBytes(capitalizedSentence);
        }
    }

    @Override
    public void ShutDownServer()
    {
        m_bShutdown = true;
    }
}

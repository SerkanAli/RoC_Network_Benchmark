package RoC.NetworkProtocolsBench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
    public void ListentoPort() throws IOException
    {
        System.out.println("Listen to port TCP...");
        ServerSocket oServerSocket = new ServerSocket(m_nPort);
        Socket oConnectedSocket = oServerSocket.accept();
        InetAddress oAdr = oConnectedSocket.getInetAddress();
        while (!m_bShutdown)
        {
            /*if(!m_bBeginIsSet)
            {
                m_nBeginTime = BenchNetworkTime.GetCurrentTime();
                m_bBeginIsSet = true;
            }*/
            BufferedReader oMsgClient =new BufferedReader(new InputStreamReader(oConnectedSocket.getInputStream()));
            m_aTimes.add(BenchNetworkTime.GetCurrentTime());
            String sBuf = oMsgClient.readLine();
            System.out.println(sBuf.substring(0,2));
        }
    }

    @Override
    public void ShutDownServer()
    {
        m_bShutdown = true;
    }


}

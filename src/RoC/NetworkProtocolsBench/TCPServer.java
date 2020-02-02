package RoC.NetworkProtocolsBench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        int nI = 0;

        System.out.println("Listen to port TCP...");
        ServerSocket oServerSocket = new ServerSocket(m_nPort + nI);
        nI++;
        System.out.println("is TCP closed? :" + oServerSocket.isClosed());
        Socket oConnectedSocket = oServerSocket.accept();
        System.out.println("Conected");
        while (!m_bShutdown) {
    /*if(!m_bBeginIsSet)
    {
        m_nBeginTime = BenchNetworkTime.GetCurrentTime();
        m_bBeginIsSet = true;
    }*/
            System.out.println("Revie data");
            BufferedReader oMsgClient = new BufferedReader(new InputStreamReader(oConnectedSocket.getInputStream()));
            //  m_aTimes.add(BenchNetworkTime.GetCurrentTime());
            String sBuf = oMsgClient.readLine();
            if (sBuf == null)
                break;
            System.out.println(sBuf.substring(0, 4));
        }
        oServerSocket.close();
        System.out.println("Closing");
    }

    @Override
    public boolean NeedLoop() {
        return true;
    }

    @Override
    public void ShutDownServer()
    {
        m_bShutdown = true;
    }


}

package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.openjdk.jmh.runner.RunnerException;

import java.lang.management.ThreadMXBean;
import java.security.AccessControlException;

public class Main {

    private static boolean m_bUseClient = true; //true: this jar is client side/ false: this jar is Server side
    private static short m_nProtocol = 0; // 0 = TCP / 1 = UDP ...


    public static void main(String[] args) {
        BenchNetworkTime.GetCurrentTime();
        if(m_bUseClient)
        {
            UseClient();
        }
        else
        {
           UseServer();
        }

    }

    private static void UseClient()
    {
        BaseClient oClient;
        if(m_nProtocol == 0 /*is TCP*/)
        {
            oClient = new TCPClient();
        }
        else if(m_nProtocol == 1 /*is UDP*/)
        {
            oClient = new UDPClient();
        }
        else if(m_nProtocol == 2)
        {
            oClient = new MQTTClient();
        }
        else
            oClient = new UDPClient();

        oClient.SetPort(6300);
        //oClient.SetIPAdress("169.254.41.185"); //eth
        oClient.SetIPAdress("192.168.178.45"); //wlan

        BenchNetwork oBench = new BenchNetwork();
        try {
            oBench.BeginBenchmark(oClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void UseServer()
    {
        BaseServer oServer;
        if(m_nProtocol == 0)
        {
            oServer = new TCPServer();

        }
        else if(m_nProtocol == 1)
        {
            oServer = new UDPServer();
        }
        else
        {
            oServer = new MQTTServer();
        }


        oServer.SetPort(6300);
        try {
            oServer.ListentoPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void printUsage()
    {

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        System.out.println( "System Load: "+operatingSystemMXBean.getProcessCpuTime() );

    }
}
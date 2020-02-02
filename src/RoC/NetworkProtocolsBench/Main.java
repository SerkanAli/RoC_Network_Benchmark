package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;


public class Main {

    private static boolean m_bUseClient = false; //true: this jar is client side/ false: this jar is Server side
    private static short m_nProtocol = 1; // 0 = TCP / 1 = UDP / 2 = MQTT / 3 = Zigbee ...
    private static String m_sIP = "192.168.178.";


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
      BenchNetworkThreadPool oBenchClient = new BenchNetworkThreadPool(m_sIP, m_nProtocol);
      oBenchClient.BeginBench();
    }

    private static void UseServer()
    {
        ThreadPooledServer oServer = new ThreadPooledServer();
        new Thread(oServer).start();
    }


    private static void printUsage()
    {

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        System.out.println( "System Load: "+operatingSystemMXBean.getProcessCpuTime() );

    }
}
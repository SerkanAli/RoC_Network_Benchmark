package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

class Parameter{
    static short m_nThreadCountMin = 2;
    static short m_nThreadCountMax = 2;
    static short m_nIterationCount = 1;
    static float m_nFileSizeMin = 0.01F;
    static float m_nFileSizeMax = 2F;
    static int m_nSleepTime = 600;
}

public class Main {

    private static boolean m_bUseClient = false; //true: this jar is client side/ false: this jar is Server side
    private static String m_sIP = "192.168.178.58";


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
      BenchNetworkThreadPool oBenchClient = new BenchNetworkThreadPool(m_sIP);
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
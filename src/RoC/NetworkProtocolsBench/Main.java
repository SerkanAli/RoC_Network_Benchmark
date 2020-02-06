package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

class Parameter{
    static byte m_nThreadCountMin = 1;
    static byte m_nThreadCountMax = 16;
    static byte m_nThreadIncrease = 2;
    static byte m_nIterationCount = 16;
    static float m_nFileSizeMin = 0.01F;
    static float m_nFileSizeMax =45F;
    static int m_nSleepTime = 60000;
}

public class Main {

    private static boolean m_bUseClient = true; //true: this jar is client side/ false: this jar is Server side
    //private static String m_sIP = "169.254.41.185";
   private static String m_sIP = "192.168.178.60";
   //private static String m_sIP = "192.168.178.45";

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
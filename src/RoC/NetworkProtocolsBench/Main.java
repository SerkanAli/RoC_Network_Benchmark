package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

class Parameter{
    static byte m_nThreadCountMin = 1;
    static byte m_nThreadCountMax = 1;
    static byte m_nThreadIncrease = 1;
    static int m_nIterationCount = 40;
    static int m_nIterationDivider = 2;
    static float m_nFileSizeMin = 1F;
    static float m_nFileSizeMax =5F;
    static int m_nSleepTime = 5000;
    static int m_nProtocol = 0; //0 TCP, 1 UDP, 2 MQTT, 3 = All protocols
}

public class Main {

    private static boolean m_bUseClient = true; //true: this jar is client side/ false: this jar is Server side
  //   private static String m_sIP = "169.254.41.185";
  // private static String m_sIP = "192.168.178.60";
     private static String m_sIP = "192.168.178.45";
  //private static String m_sIP = "169.254.41.185";
    static int m_nPro = 0;
    static byte m_nThreads = 0;
    public static void main(String[] args) {
      //  m_nPro = Integer.parseInt(args[0]);
       // m_nThreads = Byte.parseByte(args[1]);
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
        for(int nPro = 0; nPro < 3; nPro++) {
            if(nPro == Parameter.m_nProtocol || Parameter.m_nProtocol == 3) {
                for (int nCount = 0; nCount < 5; nCount++) {
                    BenchNetworkThreadPool oBenchClient = new BenchNetworkThreadPool(m_sIP);
                    oBenchClient.BeginBench(nPro);
                }
            }
        }
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
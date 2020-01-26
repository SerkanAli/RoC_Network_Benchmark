package RoC.NetworkProtocolsBench;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.AccessControlException;
import java.util.Random;

public class BenchNetworkTime {

    private BaseClient m_oClient;

    private long m_nBeginTime;

    long m_nStartTime;
    long m_nLastTime;
    long m__nLastThreadTime;

    float m_nSmoothLoad = 0;
    long m_nTotalTime;
    ThreadMXBean m_oNewBean;

    public BenchNetworkTime(BaseClient oClient)
    {
        m_oClient = oClient;
    }

    public static long GetCurrentTime()
    {
        return TimeStamp.getCurrentTime().ntpValue();
    }

    public void Begin()
    {
        m_nBeginTime = GetCurrentTime();


        /*
         *
         *   CPU usage
         *
         * */
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        m_oNewBean = ManagementFactory.getThreadMXBean();
        try
        {
            if (m_oNewBean.isThreadCpuTimeSupported())
                m_oNewBean.setThreadCpuTimeEnabled(true);
            else
                throw new AccessControlException("");
        }
        catch (AccessControlException e)
        {
            System.out.println("CPU Usage monitoring is not available!");
            System.exit(0);
        }
        m_nStartTime = System.nanoTime();
        m_nLastTime = System.nanoTime();
        m__nLastThreadTime = m_oNewBean.getCurrentThreadCpuTime();

        m_nSmoothLoad = 0;
    }

    private long m_nThroughput;
    private long m_nLatency;

    public void End(long nFileSize)
    {
        long nEndTime = GetCurrentTime();
        long nServerBeginTime = m_oClient.GetServerBeginTime();
        m_nLatency = nServerBeginTime - m_nBeginTime;
        if(m_nLatency < 0)
            m_nLatency = 0;
        m_nThroughput = (nEndTime - m_nBeginTime + m_nLatency) / nFileSize;

        // Calculate coarse CPU usage:
        long time = System.nanoTime();
        long threadTime = m_oNewBean.getCurrentThreadCpuTime();
        double load = (threadTime - m__nLastThreadTime) / (double)(time - m_nLastTime);
        // Smooth it.
        m_nSmoothLoad += (load - m_nSmoothLoad) * 0.1; // damping factor, lower means less responsive, 1 means no smoothing.

        // For next iteration.
        m_nLastTime = time;
        m__nLastThreadTime = threadTime;


        m_nTotalTime = System.nanoTime() - m_nStartTime;
    }

    public long GetTroughput()
    {
        return m_nThroughput;
    }


    public long GetLatency()
    {
        return m_nLatency;
    }

    public long GetTotalTime()
    {
        return m_nTotalTime;
    }

    public float GetCPULoad()
    {
        return m_nSmoothLoad;
    }
}


class BenchNetwork
{

    public void BeginBenchmark(BaseClient oCLient) throws IOException {
        String s5Mbyte = createDataSize(5);
        String s100Mybte = createDataSize(100);
        oCLient.CreateConnection();

        BenchNetworkTime oTime = new BenchNetworkTime(oCLient);



        /*
        *    Transfer 5 MB
        * */
        oTime.Begin();
        oCLient.SendStringOverConnection(s5Mbyte);
        oTime.End(5 * 1048576);

        System.out.println("Result of 5 Mybte transfer is:");
        System.out.println("Total time: " + oTime.GetTotalTime());
        System.out.println("Throughput: " + oTime.GetTroughput());
        System.out.println("CPU Load: " + oTime.GetCPULoad());
        System.out.println("");

        oTime.Begin();
        oCLient.SendStringOverConnection(s100Mybte);
        oTime.End(100 * 1048576);
        System.out.println("Result of 5 Mybte transfer is:");
        System.out.println("Total time: " + oTime.GetTotalTime());
        System.out.println("Throughput: " + oTime.GetTroughput());
        System.out.println("CPU Load: " + oTime.GetCPULoad());
        System.out.println("");
    }



    private static String createDataSize(int nMegabyte) {
        int msgSize = nMegabyte * 524288;
        StringBuilder sb = new StringBuilder(msgSize);
        Random rnd = new Random();
        for (int i=0; i<msgSize; i++) {
            sb.append((char) (rnd.nextInt(26) + 'a'));
        }
        return sb.toString();
    }



}

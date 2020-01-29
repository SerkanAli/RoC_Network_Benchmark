package RoC.NetworkProtocolsBench;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.Random;

public class BenchNetworkTime {

    private BaseClient m_oClient;

    private double m_nThroughput;
    private long m_nLatency;

    private long m_nStartSendDataTime;

    private long m_nStartProcessTime;
    private long m_nLastTime;
    private long m__nLastThreadTime;

    private float m_nSmoothLoad = 0;
    private long m_nTotalTime;
    ThreadMXBean m_oNewBean;

    public BenchNetworkTime(BaseClient oClient)
    {
        m_oClient = oClient;
    }

    public static long GetCurrentTime()
    {
        return TimeStamp.getCurrentTime().getTime();
    }

    public void Begin()
    {
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
        m_nStartProcessTime = System.nanoTime();
        m_nLastTime = System.nanoTime();
        m__nLastThreadTime = m_oNewBean.getCurrentThreadCpuTime();

        m_nSmoothLoad = 0;

        m_nThroughput = 0;
    }

    public void Next_BeforeSend()
    {
        m_nStartSendDataTime = GetCurrentTime();
    }

    public void Next_AfterSend()
    {
        m_nThroughput = m_nThroughput + GetCurrentTime() - m_nStartSendDataTime;
        // Calculate coarse CPU usage:
        long time = System.nanoTime();
        long threadTime = m_oNewBean.getCurrentThreadCpuTime();
        double load = (threadTime - m__nLastThreadTime) / (double)(time - m_nLastTime);
        // Smooth it.
        m_nSmoothLoad += (load - m_nSmoothLoad) * 0.1; // damping factor, lower means less responsive, 1 means no smoothing.

        // For next iteration.
        m_nLastTime = time;
        m__nLastThreadTime = threadTime;
    }

    public void End(long nFileSize)
    {
        long nEndTime = GetCurrentTime();
        long nServerBeginTime = m_oClient.GetServerBeginTime();
        m_nLatency = nServerBeginTime - m_nStartSendDataTime;
        if(m_nLatency < 0)
            m_nLatency = 0;


        m_nTotalTime = System.nanoTime() - m_nStartProcessTime;
    }

    public double GetTroughput()
    {
        return m_nThroughput /1000;
    }


    public long GetLatency()
    {
        return m_nLatency;
    }

    public float GetTotalTime()
    {
        return (float)m_nTotalTime / 1000000000;
    }

    public float GetCPULoad()
    {
        return m_nSmoothLoad * 100;
    }
}


class BenchNetwork
{

    public void BeginBenchmark(BaseClient oCLient) throws IOException {
      //  oCLient.CreateConnection();
        //Warm up
      //  for(int nCount = 0; nCount < 10; nCount++)
        //    oCLient.SendStringOverConnection("Warm Up");

        //Actual Benchmark with different Mbyte sizes
        OneBench(1, oCLient);
        OneBench(2, oCLient);
        OneBench(5, oCLient);
        OneBench(10, oCLient);
        OneBench(25, oCLient);
        OneBench(50, oCLient);
    }

    void OneBench(int size, BaseClient oCLient) throws IOException {
        BenchNetworkTime oTime = new BenchNetworkTime(oCLient);
        String sData = createDataSize(size);
        final short nIterationCount = 10;
        oTime.Begin();
        oCLient.CreateConnection();
        for(int nCount = 0; nCount < nIterationCount; nCount++) {
            oTime.Next_BeforeSend();
            oCLient.SendStringOverConnection(sData);
            oTime.Next_AfterSend();
        }
        oCLient.CloseConnection();
        oTime.End(size * nIterationCount * 1048576);

        System.out.println("Result of "+ size +" Mybte transfer at "+ nIterationCount+" itarations is:");
        System.out.println("Total time: " + new DecimalFormat("###.##").format( oTime.GetTotalTime()) + " sec");
        System.out.println("Throughput: " +new DecimalFormat("###.##").format( oTime.GetTroughput()) + " Mbyte per Sec");
        System.out.println("CPU Load: " + new DecimalFormat("##.##").format(oTime.GetCPULoad()) + " %");
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

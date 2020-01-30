package RoC.NetworkProtocolsBench;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BenchNetworkTime {

    private BaseClient m_oClient;

    private double m_nThroughput;
    private long m_nLatency;
    private float m_nSmoothLoad = 0;
    private long m_nTotalTime;
    private double m_nTotalUsage;
    private double m_nAvgCoreUsage;


    private long m_nStartSendDataTime;
    private long m_nStartProcessTime;
    private long m__nLastThreadTime;

    ThreadMXBean m_oNewBean;
    MonitoringThread m_oMonitor;

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
        m_oMonitor = new MonitoringThread(10);
        m_oMonitor.run();
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
        m__nLastThreadTime = m_oNewBean.getCurrentThreadCpuTime();

        m_nSmoothLoad = 0;

        m_nThroughput = 0;
    }

    public void Next_BeforeSend()
    {
        m_nStartSendDataTime = GetCurrentTime();
    }

    public void Next_AfterSend() {  m_nThroughput = m_nThroughput + GetCurrentTime() - m_nStartSendDataTime; }

    public void End(float nFileSize)
    {
        long nEndTime = GetCurrentTime();
        long nServerBeginTime = m_oClient.GetServerBeginTime();
        m_nLatency = nServerBeginTime - m_nStartSendDataTime;
        if(m_nLatency < 0)
            m_nLatency = 0;
        m_oMonitor.stopMonitor();
        m_nTotalUsage = m_oMonitor.getTotalUsage();
        m_nAvgCoreUsage = m_oMonitor.getAvarageUsagePerCPU();
        m_nTotalTime = System.nanoTime() - m_nStartProcessTime;
        m_nSmoothLoad = (m_oNewBean.getCurrentThreadCpuTime() - m__nLastThreadTime) / (float)m_nTotalTime;
        m_nThroughput = nFileSize / m_nThroughput;
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

    public double GetTotalUsage() {return m_nTotalUsage;}
    public double GetAvgCoreUsage() {return m_nAvgCoreUsage;}
}


class BenchNetwork
{

    public void BeginBenchmark(BaseClient oCLient) throws IOException {
            for(float nFileSize = 0.01F; nFileSize < 100; nFileSize = nFileSize * 2)
            {
                for(short nIterations = 1; nIterations < 20; nIterations = (short) (nIterations + 5))
                {
                    OneBench(nFileSize, nIterations, oCLient);
                }
            }
            WriteResultstoFile("OverWifi");
    }

    private void OneBench(float nFileSize, short nIteration, BaseClient oCLient) throws IOException {
        BenchNetworkTime oTime = new BenchNetworkTime(oCLient);
        String sData = createDataSize(nFileSize);
        oTime.Begin();
        oCLient.CreateConnection();
        for(int nCount = 0; nCount < nIteration; nCount++) {
            oTime.Next_BeforeSend();
            oCLient.SendStringOverConnection(sData);
            oTime.Next_AfterSend();
        }
        oCLient.CloseConnection();
        oTime.End(nFileSize * nIteration * 1048576);

        System.out.println("Result of "+ nFileSize +" Mybte transfer at "+ nIteration+" itarations is:");
        System.out.println("Total time: " + new DecimalFormat("###.##").format( oTime.GetTotalTime()) + " sec");
        System.out.println("Throughput: " +new DecimalFormat("###.##").format( oTime.GetTroughput()) + " Mbyte per Sec");
        System.out.println("CPU Load: " + new DecimalFormat("##.##").format(oTime.GetCPULoad()) + " %");
        System.out.println("Total Usage: " + new DecimalFormat("##.##").format(oTime.GetTotalUsage()) + " %");
        System.out.println("Avg Core Load: " + new DecimalFormat("##.##").format(oTime.GetAvgCoreUsage()) + " %");
        System.out.println("");

        CachingResults(oCLient.GetProtocolName(), String.valueOf(nFileSize), String.valueOf(nIteration), oTime);
    }


    private static String createDataSize(float nMegabyte) {
        int msgSize = Math.round(nMegabyte * 524288F);
        StringBuilder sb = new StringBuilder(msgSize);
        Random rnd = new Random();

        for (int i=0; i<msgSize; i = i + 4) {
           // sb.append((char) (rnd.nextInt(26) + 'a'));
            sb.append('H');
            sb.append('e');
            sb.append('y');
            sb.append('x');
        }
        return sb.toString();
    }

    List<List<String>> m_aResults = new ArrayList<>();

    private void CachingResults(String sProtocol, String sFileSize, String sIteration, BenchNetworkTime oTime)
    {
        List<String> oRow = new ArrayList<String>(); ;
        oRow.add(sProtocol);
        oRow.add(sFileSize);
        oRow.add(sIteration);
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTotalTime()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTroughput()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetCPULoad()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetAvgCoreUsage()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTotalUsage()));
        m_aResults.add(oRow);
    }


    public void WriteResultstoFile( String sFilname)
    {
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(sFilname + ".csv");
            csvWriter.append("Protocol");
            csvWriter.append(",");
            csvWriter.append("Filesize");
            csvWriter.append(",");
            csvWriter.append("Iterations");
            csvWriter.append(",");
            csvWriter.append("Total Time");
            csvWriter.append(",");
            csvWriter.append("Throughput");
            csvWriter.append(",");
            csvWriter.append("Thread Load");
            csvWriter.append(",");
            csvWriter.append("Avg Core Load");
            csvWriter.append(",");
            csvWriter.append("Total Usage");
            csvWriter.append("\n");

            for (List<String> rowData : m_aResults) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

package RoC.NetworkProtocolsBench;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.management.ManagementFactory.getPlatformMXBean;

public class BenchNetworkTime {

    private BaseClient m_oClient;

    private double m_nThroughput;
    private long m_nLatency;
    private double  m_nSmoothLoad;
    private long m_nTotalTime;
    private double m_nTotalUsage;
    private double m_nAvgCoreUsage;


    private long m_nStartSendDataTime;
    private long m_nStartProcessTime;

    MonitoringThread m_oMonitor;
    PerformanceMonitor m_oPerformance;

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
        m_oMonitor = new MonitoringThread(1000);
        m_oPerformance = new PerformanceMonitor();
        m_oPerformance.Next();
        m_nStartProcessTime = GetCurrentTime();
        m_nThroughput = 0;
    }

    public void Next_BeforeSend()
    {
        m_nStartSendDataTime = GetCurrentTime();
        m_oPerformance.Next();
    }

    public void Next_AfterSend()
    {
        m_nThroughput = m_nThroughput + GetCurrentTime() - m_nStartSendDataTime;
        m_oPerformance.Next();
    }

    public void End(float nFileSize)
    {
        m_oPerformance.Next();
        long nEndTime = GetCurrentTime();
        long nServerBeginTime = m_oClient.GetServerBeginTime();
        m_nLatency = nServerBeginTime - m_nStartSendDataTime;
        if(m_nLatency < 0)
            m_nLatency = 0;
        m_oMonitor.stopMonitor();
        m_nTotalUsage = m_oPerformance.GetTotalUsage();
        m_nAvgCoreUsage =  m_oPerformance.GetAvarageCoreusage();
        m_nTotalTime = GetCurrentTime() - m_nStartProcessTime;
        m_nSmoothLoad = m_oPerformance.GetAvarageThreadUsage();
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
        return (float)m_nTotalTime / 1000F  ;
    }

    public double GetThreadLoad()
    {
        return m_nSmoothLoad * 100;
    }

    public double GetTotalUsage() {return m_nTotalUsage * 100D;}
    public double GetAvgCoreUsage() {return m_nAvgCoreUsage * 100D;}
}

class BenchNetworkThreadPool
{
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    protected Thread       runningThread= null;
    protected String m_sIPAdress = "";
    protected  int m_nProtocol;
    BenchNetworkThreadPool(String IPAdress, int nProtocol)
    {
        m_sIPAdress = IPAdress;
        m_nProtocol = nProtocol;

        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
    }

    public void BeginBench()
    {
        List<BenchNetwork> aClientList = new ArrayList<>();
        int nPort = 0;
        if(m_nProtocol == 0)
            nPort = 6300;
        else
            nPort = 63005;

        for(int nCount = 0; nCount < 5; nCount++)
        {
            BaseClient oClient;
            if (m_nProtocol == 0 /*is TCP*/) {
                oClient = new TCPClient();
            } else if (m_nProtocol == 1 /*is UDP*/) {
                oClient = new UDPClient();
            } else if (m_nProtocol == 2) {
                oClient = new MQTTClient();
            } else
                oClient = new UDPClient();

            oClient.SetPort(nPort + nCount);
            oClient.SetIPAdress(m_sIPAdress); //wlan
             BenchNetwork oClientBench = new BenchNetwork(oClient, "5", String.valueOf(nCount));
            this.threadPool.execute(oClientBench);
            aClientList.add(oClientBench);
        }

        List<List<String>> aResults = new ArrayList<>();
        for(int nIndex = 0; nIndex < aClientList.size(); nIndex++) {
               while (aClientList.get(nIndex).IsRunnign()) {
                   try {
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
               aResults.addAll(aClientList.get(nIndex).GetResults());
        }

        WriteResultstoFile("5ThreadTCPOverWifi", aResults);

    }

    public void WriteResultstoFile( String sFilname,  List<List<String>> aResults)
    {
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(sFilname + ".csv");
            csvWriter.append("Protocol");
            csvWriter.append(",");
            csvWriter.append("Filesize");
            csvWriter.append(",");
            csvWriter.append("ThreadCount");
            csvWriter.append(",");
            csvWriter.append("ThreadIndex");
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

            for (List<String> rowData : aResults) {
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


class BenchNetwork implements Runnable
{
    protected  BaseClient m_oClient;
    protected  String m_sThreadCount;
    protected  String m_sThreadIndex;
    protected  boolean m_bIsRunning = true;
    BenchNetwork(BaseClient oClient, String sThreadCount, String sThreadIndex)
    {
        m_oClient = oClient;
        m_sThreadCount = sThreadCount;
        m_sThreadIndex = sThreadIndex;
    }
    public boolean IsRunnign()
    {
        return m_bIsRunning;
    }
    @Override
    public void run() {
        try {
            BeginBenchmark(m_oClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_bIsRunning = false;
    }

    protected void BeginBenchmark(BaseClient oCLient) throws IOException {
            for(float nFileSize = 1F; nFileSize < 40; nFileSize = nFileSize * 2)
            {
                for(short nIterations = 1; nIterations < 7; nIterations = (short) (nIterations + 5))
                {
                    OneBench(nFileSize, nIterations, oCLient);
                }
            }
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
        System.out.println("Thread Load: " + new DecimalFormat("##.##").format(oTime.GetThreadLoad()) + " %");
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
        oRow.add(m_sThreadCount);
        oRow.add(m_sThreadIndex);
        oRow.add(sIteration);
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTotalTime()).replace(',','.'));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTroughput()).replace(',','.'));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetThreadLoad()).replace(',','.'));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetAvgCoreUsage()).replace(',','.'));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTotalUsage()).replace(',','.'));
        m_aResults.add(oRow);
    }

    public List<List<String>> GetResults()
    {
        return m_aResults;
    }


}

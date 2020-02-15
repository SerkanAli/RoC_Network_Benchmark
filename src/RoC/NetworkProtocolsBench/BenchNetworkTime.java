package RoC.NetworkProtocolsBench;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.management.ManagementFactory.getPlatformMXBean;

public class BenchNetworkTime {

    private BaseClient m_oClient;
    private Semaphore m_oSemaphore;

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

    public BenchNetworkTime(Semaphore semaphore)
    {
        m_oSemaphore = semaphore;
    }

    public static long GetCurrentTime()
    {
        return TimeStamp.getCurrentTime().getTime();
    }

    public void Begin()
    {
        m_nStartProcessTime = GetCurrentTime();
        m_oMonitor = new MonitoringThread();
        m_oPerformance = new PerformanceMonitor(m_oSemaphore);
        m_oPerformance.Next();
        m_nThroughput = 0;
    }

    public long CreatedConnection()
    {
        return GetCurrentTime() - m_nStartProcessTime;
    }

    public void Next_BeforeSend()
    {
        m_nStartSendDataTime = GetCurrentTime();
    }

    public void Next_AfterSend(boolean bHasSend)
    {
        if(bHasSend)
            m_nThroughput = m_nThroughput + GetCurrentTime() - m_nStartSendDataTime;
    }

    public void End(float nFileSize)
    {
        m_oPerformance.Next();
        m_nLatency = 0;
        m_oMonitor.stopMonitor();
        m_nTotalUsage = m_oPerformance.GetTotalUsage();
        m_nAvgCoreUsage = m_oPerformance.GetAverageCoreLoad();
        m_nTotalTime = GetCurrentTime() - m_nStartProcessTime;
        m_nSmoothLoad = m_oMonitor.getUsageByThread(Thread.currentThread());
        //m_nThroughput = nFileSize / (GetCurrentTime()-m_nStartProcessTime);
        m_nThroughput = nFileSize / m_nThroughput;
    }

    public double GetTroughput()
    {
        return m_nThroughput /1000D;
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
        return m_nSmoothLoad;
    }

    public double GetTotalUsage() {return m_nTotalUsage * 100D;}
    public double GetAvgCoreUsage() {return m_nAvgCoreUsage *100D ;}
}

class BenchDataSet{
    public Map<Float, byte[]> data = new TreeMap<Float, byte[]>();

    BenchDataSet(){
        byte nID = 1;
        for(float nSize = Parameter.m_nFileSizeMin; nSize < Parameter.m_nFileSizeMax; nSize = nSize * 2) {
            data.put(nSize, createDataSize(nSize, nID));
            nID++;
        }
    }
    private static byte[] createDataSize(float nMegabyte, byte nID) {
        int msgSize = Math.round(nMegabyte * 1048576F);
        byte[] aData = new byte[msgSize];
        aData[0] = nID;
        for (int i=1; i+4<msgSize; i = i + 4) {
            aData[i] = (byte)'H';
            aData[i+1] = (byte)'e';
            aData[i+2] = (byte)'y';
            aData[i+3] = (byte)'!';
        }
        return aData;
    }
}

class BenchNetworkThreadPool
{
    protected ExecutorService threadPool = Executors.newFixedThreadPool(4);
    protected String m_sIPAdress = "";

    BenchNetworkThreadPool(String IPAdress)
    {
        m_sIPAdress = IPAdress;

    }

    protected  BenchDataSet m_aDataSet;
    public void BeginBench(int m_nProtocol/*int nIterations, int ndivider, int m_nProtocol, byte nThreadCount*/)
    {
        //Create once same Dataset for all Thread, so Memory heap is avoided
        m_aDataSet = new BenchDataSet();


            //Create Csv File to store the results
        {
            String sFileName;
            if (m_nProtocol == 0) sFileName = "TCP";
            else if (m_nProtocol == 1) sFileName = "UDP";
            else sFileName = "MQTT";
            sFileName = sFileName + " OverEth" + new DecimalFormat("#####").format( BenchNetworkTime.GetCurrentTime());
            CreateFile(sFileName);

            //Test different counts of threads
            Semaphore semaphoreLoad = new Semaphore(1);
            for (byte nThreadCount = Parameter.m_nThreadCountMin; nThreadCount <= Parameter.m_nThreadCountMax; nThreadCount = (byte) (nThreadCount + Parameter.m_nThreadIncrease)) {
                int nIterations = Parameter.m_nIterationCount;
                for(Float nSize = Parameter.m_nFileSizeMin; nSize < Parameter.m_nFileSizeMax; nSize = nSize * 2) {{
                        BenchNetworkTime oTime = new BenchNetworkTime(semaphoreLoad);
                        oTime.Begin();
                       // Create Clients and Threads
                        List<BenchNetwork> aClientList = new ArrayList<>();
                        int nPort = 0;
                        if (m_nProtocol == 0)
                            nPort = 6290;
                        else
                            nPort = 6310;

                        for (int nCount = 0; nCount < nThreadCount; nCount++) {
                            BaseClient oClient;
                            if (m_nProtocol == 0 /*is TCP*/) {
                                oClient = new TCPClient();
                            } else if (m_nProtocol == 1 /*is UDP*/) {
                                oClient = new UDPClient();
                            } else {
                                oClient = new MQTTClient();
                            }

                            oClient.SetPort(nPort + nCount);
                            oClient.SetIPAdress(m_sIPAdress); //wlan
                            BenchNetwork oClientBench = new BenchNetwork(oClient, String.valueOf(nThreadCount), String.valueOf(nCount), semaphoreLoad, m_aDataSet, nSize, nIterations);
                            nIterations = (nIterations / Parameter.m_nIterationDivider);
                            aClientList.add(oClientBench);
                        }
                        long nConTime = 0;
                        try {
                            List<Future<List<List<String>>>> futures = threadPool.invokeAll(aClientList);
                            nConTime = oTime.CreatedConnection();

                        for (int nIndex = 0; nIndex < futures.size(); nIndex++) {

                            try {
                                WriteResultstoFile(sFileName, futures.get(nIndex).get(2,TimeUnit.MINUTES));
                            } catch (ExecutionException | TimeoutException e) {
                                List<List<String>> aRes = new ArrayList<>();
                                List<String> oRow = new ArrayList<String>(); ;
                                oRow.add(String.valueOf(m_nProtocol));
                                oRow.add(String.valueOf(nSize));
                                oRow.add(String.valueOf(nThreadCount));
                                oRow.add(String.valueOf(nIndex));
                                oRow.add(String.valueOf(nIterations));
                                oRow.add("-1");
                                oRow.add("-1");
                                oRow.add("-1");
                                oRow.add("-1");
                                oRow.add("-1");
                                oRow.add("-1");
                                aRes.add(oRow);
                               WriteResultstoFile(sFileName,aRes);
                            }
                        }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        oTime.End(nSize*nIterations*1048576);
                        List<List<String>> aRes = new ArrayList<>();
                        List<String> oRow = new ArrayList<String>(); ;
                        oRow.add(String.valueOf(m_nProtocol));
                        oRow.add(new DecimalFormat("#######").format(nSize));
                        oRow.add(String.valueOf(nThreadCount));
                        oRow.add(String.valueOf(0));
                        oRow.add(String.valueOf(nIterations));
                        oRow.add(new DecimalFormat("###.####").format(oTime.GetTotalTime()));
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add("-2");
                        oRow.add(String.valueOf(nConTime));
                        aRes.add(oRow);
                        WriteResultstoFile(sFileName,aRes);
                    }
                    try {
                        System.out.println("\n Sleping for while\n");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    System.out.println("\n Sleping for 5 sec\n");
                    Thread.sleep(Parameter.m_nSleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void CreateFile(String sFilename){
        try {
            FileWriter csvWriter = new FileWriter(sFilename + ".csv");
            csvWriter.append("Protocol");
            csvWriter.append(":");
            csvWriter.append("Filesize");
            csvWriter.append(":");
            csvWriter.append("ThreadCount");
            csvWriter.append(":");
            csvWriter.append("ThreadIndex");
            csvWriter.append(":");
            csvWriter.append("Iterations");
            csvWriter.append(":");
            csvWriter.append("Total Time");
            csvWriter.append(":");
            csvWriter.append("Throughput");
            csvWriter.append(":");
            csvWriter.append("Thread Load");
            csvWriter.append(":");
            csvWriter.append("Avg Core Load");
            csvWriter.append(":");
            csvWriter.append("Total Usage");
            csvWriter.append(":");
            csvWriter.append("Bench ID");
            csvWriter.append(":");
            csvWriter.append("Failed");
            csvWriter.append(":");
            csvWriter.append("Connection Time");
            csvWriter.append("\n");
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void WriteResultstoFile( String sFilename,  List<List<String>> aResults)
    {
        try {
            FileWriter csvWriter = new FileWriter(sFilename + ".csv", true);
            for (List<String> rowData : aResults) {
                csvWriter.append(String.join(":", rowData));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


class BenchNetwork implements Callable<List<List<String>>> {
    protected  BaseClient m_oClient;
    protected  String m_sThreadCount;
    protected  String m_sThreadIndex;
    protected  boolean m_bIsRunning = true;
    protected   Semaphore m_oSemaphoreLoad;
    protected BenchDataSet m_aDataSet;
    protected float m_nFileSize;
    protected int m_nIteration;
    BenchNetwork(BaseClient oClient, String sThreadCount, String sThreadIndex,  Semaphore semaphoreLoad , BenchDataSet oData, float nSize, int nIter)
    {
        m_oClient = oClient;
        m_sThreadCount = sThreadCount;
        m_sThreadIndex = sThreadIndex;
        m_oSemaphoreLoad = semaphoreLoad;
        m_aDataSet = oData;
        m_nFileSize = nSize;
        m_nIteration = nIter;
    }
    public boolean IsRunnign()
    {
        return m_bIsRunning;
    }
    //@Override
    public void run() {
        try {
            OneBench(m_oClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_bIsRunning = false;
    }



    private void OneBench(BaseClient oCLient) throws IOException {
        Random rnd = new Random();
        int nFailed=0;
        long nBenchID = rnd.nextLong();
        System.out.println("Bench ID:" + nBenchID);
        BenchNetworkTime oTime = new BenchNetworkTime(m_oSemaphoreLoad);
        oTime.Begin();
        oCLient.CreateConnection();
        final long nConnectionTime = oTime.CreatedConnection();
        for(int nCount = 0; nCount < m_nIteration; nCount++) {
            oTime.Next_BeforeSend();
            if(!oCLient.SendStringOverConnection(m_nFileSize, m_aDataSet, nBenchID))
            {
                nFailed++;
                oTime.Next_AfterSend(false);
            }
            else
                oTime.Next_AfterSend(true);
        }
        oCLient.CloseConnection();
        oTime.End(m_nFileSize * m_nIteration * 1048576);

        System.out.println("Result of "+ oCLient.GetProtocolName()+" "+ m_nFileSize +" Mybte transfer at "+ m_nIteration+" itarations in Thread "+ m_sThreadIndex + "/"+ m_sThreadCount +" is:");
        System.out.println("Total time: " + new DecimalFormat("###.##").format( oTime.GetTotalTime()) + " sec");
        System.out.println("Throughput: " +new DecimalFormat("###.####").format( oTime.GetTroughput()) + " Mbyte per Sec");
        System.out.println("Thread Load: " + new DecimalFormat("##.##").format(oTime.GetThreadLoad()) + " %");
        System.out.println("Total Usage: " + new DecimalFormat("##.##").format(oTime.GetTotalUsage()) + " %");
        System.out.println("Failed: " +String.valueOf(nFailed) + " %");
        System.out.println("");

        CachingResults(oCLient.GetProtocolName(), new DecimalFormat("###.##").format( m_nFileSize), String.valueOf(m_nIteration), oTime, nBenchID, nFailed, nConnectionTime);

    }

    List<List<String>> m_aResults = new ArrayList<>();

    private void CachingResults(String sProtocol, String sFileSize, String sIteration, BenchNetworkTime oTime, long nBenchID, int nFail, long nConnectionTime)
    {
        List<String> oRow = new ArrayList<String>(); ;
        oRow.add(sProtocol);
        oRow.add(sFileSize);
        oRow.add(m_sThreadCount);
        oRow.add(m_sThreadIndex);
        oRow.add(sIteration);
        oRow.add(new DecimalFormat("###.####").format(oTime.GetTotalTime()));
        oRow.add(new DecimalFormat("###.####").format(oTime.GetTroughput()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetThreadLoad()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetAvgCoreUsage()));
        oRow.add(new DecimalFormat("###.##").format(oTime.GetTotalUsage()));
        oRow.add(String.valueOf(nBenchID));
        oRow.add(String.valueOf(nFail));
        oRow.add(String.valueOf(nConnectionTime));
        m_aResults.add(oRow);
    }

    public List<List<String>> GetResults()
    {
        return m_aResults;
    }


    @Override
    public List<List<String>> call() throws Exception {
        run();
        return m_aResults;
    }
}

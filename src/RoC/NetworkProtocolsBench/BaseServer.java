package RoC.NetworkProtocolsBench;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public interface BaseServer {

    void SetPort(int nPort);

    void ListentoPort(WorkerRunnable.LatencyWriter oWriter) ;

    boolean NeedLoop ();


}


 class WorkerRunnable implements Runnable{
    protected  int m_nProtocol;
    protected  int m_nPort;
    protected  BaseServer m_oServer;

    public WorkerRunnable(int nProtocol, int nPort) {
        m_nProtocol = nProtocol;
        m_nPort = nPort;
    }

     @Override
    public void run() {
        boolean bNeedLoop;
         LatencyWriter oWriter = new LatencyWriter("TCPandUDPServerTimes"+ String.valueOf(BenchNetworkTime.GetCurrentTime()));
        do {
            BaseServer oServer;
            if(m_nProtocol == 0)
            {
                oServer = new TCPServer();

            }
            else if(m_nProtocol == 1)
            {
                oServer = new UDPServer();
            }
            else if(m_nProtocol == 2)
            {
                oServer = new MQTTServer();
            }
            else
                oServer = new TCPServer();

            oServer.SetPort(m_nPort);

            oServer.ListentoPort(oWriter);

            System.out.println("Peace and out");
            bNeedLoop = oServer.NeedLoop();

            oServer = null;
        }while(bNeedLoop);
    }
    static class LatencyWriter {


       String m_sFileName;
       LatencyWriter(String sFilename)
       {
           m_sFileName = sFilename;
           try {
               FileWriter csvWriter = new FileWriter(sFilename + ".csv");
               csvWriter.append("Protocol");
               csvWriter.append(":");
               csvWriter.append("Bench ID");
               csvWriter.append(":");
               csvWriter.append("Time");
               csvWriter.append(":");
               csvWriter.append("Latency");
               csvWriter.append("\n");
               csvWriter.flush();
               csvWriter.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }


        public  void WriteResultstoFile(String Prtocol, long m_nBenchID,long m_nTime, long m_nLatency) {
            try {
                FileWriter csvWriter = new FileWriter(m_sFileName + ".csv", true);

                List<String> rowData = new ArrayList<String>();
                rowData.add(Prtocol);
                rowData.add(String.valueOf(m_nBenchID));
                rowData.add(String.valueOf(m_nTime));
                rowData.add(String.valueOf(m_nLatency));
                csvWriter.append(String.join(":", rowData));
                csvWriter.append("\n");


                csvWriter.flush();
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

 class ThreadPooledServer implements Runnable{

    protected  BaseServer m_oServer;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(50);

    public ThreadPooledServer(){
       //BaseServer
    }

     @Override
    public void run()
     {

         synchronized(this){
            this.runningThread = Thread.currentThread();
        }
            //Open Servers for TCP and UDP
         final int nTCPPort = 6290;
         final int nUDPPort = 6310;
         for(int nIndex = 0; nIndex < 20; nIndex++)
         {
             this.threadPool.execute(new WorkerRunnable(0,nTCPPort + nIndex));
             this.threadPool.execute(new WorkerRunnable(1,nUDPPort + nIndex));
         }
        //Open MQTT Broker
         this.threadPool.execute(new WorkerRunnable(2,1883));

         Semaphore oSemaphore = new Semaphore(1);
         final String sFilename = "ResultsServer" + (BenchNetworkTime.GetCurrentTime()/1000);
         CreateFile(sFilename);
         while(! isStopped()){
            try {
                PerformanceMonitor oPerformance = new PerformanceMonitor(oSemaphore);
                oPerformance.Next();
                Thread.sleep(60000);
                oPerformance.Next();
                WriteResultstoFile(sFilename, oPerformance.GetNetworkInfo(), oPerformance.GetTotalUsage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }

     private static void CreateFile(String sFilename){
         try {
             FileWriter csvWriter = new FileWriter(sFilename + ".csv");
             csvWriter.append("TimeStamp");
             csvWriter.append(":");
             csvWriter.append("Interface Name");
             csvWriter.append(":");
             csvWriter.append("Recived Bytes");
             csvWriter.append(":");
             csvWriter.append("Total Load");
             csvWriter.append("\n");
             csvWriter.flush();
             csvWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
         }

     }

     private static void WriteResultstoFile(String sFilename, HashMap<String, Long> aNet, double nLoad)
     {
         try {
             FileWriter csvWriter = new FileWriter(sFilename + ".csv", true);
             Iterator it = aNet.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry pair = (Map.Entry)it.next();
                 List<String> rowData = new ArrayList<String>();
                 rowData.add(new DecimalFormat("######.##").format((float)BenchNetworkTime.GetCurrentTime()/ 1000F));
                 rowData.add(pair.getKey().toString());
                 rowData.add(pair.getValue().toString());
                 rowData.add( new DecimalFormat("##.##").format(nLoad * 100D));
                 csvWriter.append(String.join(":", rowData));
                 csvWriter.append("\n");
             }

             csvWriter.flush();
             csvWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
         }

     }

     private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
       isStopped = true;
    }


}

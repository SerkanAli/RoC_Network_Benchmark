package RoC.NetworkProtocolsBench;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public interface BaseServer {

    void SetPort(int nPort);

    void ListentoPort() throws IOException;

    boolean NeedLoop ();
    void ShutDownServer();
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
            {
                oServer = new ZigbeeServer();
            }
            oServer.SetPort(m_nPort);
            try {
                oServer.ListentoPort();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Peace and out");
            bNeedLoop = oServer.NeedLoop();
            oServer = null;
        }while(bNeedLoop);
    }
}

 class ThreadPooledServer implements Runnable{

    protected  BaseServer m_oServer;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(15);

    public ThreadPooledServer(){
       //BaseServer
    }

     @Override
    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
       // openServerSocket();
       this.threadPool.execute(new WorkerRunnable(0,6300));
        this.threadPool.execute(new WorkerRunnable(0,6301));
        this.threadPool.execute(new WorkerRunnable(0,6302));
        this.threadPool.execute(new WorkerRunnable(0,6303));
        this.threadPool.execute(new WorkerRunnable(0,6304));
        this.threadPool.execute(new WorkerRunnable(1,6305));
        this.threadPool.execute(new WorkerRunnable(1,6306));
        this.threadPool.execute(new WorkerRunnable(1,6307));
        this.threadPool.execute(new WorkerRunnable(1,6308));
        this.threadPool.execute(new WorkerRunnable(1,6309));
         this.threadPool.execute(new WorkerRunnable(2,1883));


        while(! isStopped()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
       isStopped = true;
    }


}

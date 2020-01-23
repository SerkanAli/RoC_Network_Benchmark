package RoC.NetworkProtocolsBench;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.security.AccessControlException;

public class Main {

    private static boolean m_bUseClient = false; //true: this jar is client side/ false: this jar is Server side
    private static short m_nProtocol = 0; // 0 = TCP / 1 = UDP ...


    public static void main(String[] args) {

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
        BaseClient oClient;
        if(m_nProtocol == 0 /*is TCP*/)
        {
            oClient = new TCPClient();
        }
        else if(m_nProtocol == 1 /*is UDP*/)
        {
            oClient = new UDPClient();
        }
        else if(m_nProtocol == 2)
        {
            oClient = new MQTTClient();
        }
        else
            oClient = new UDPClient();

        oClient.SetPort(6300);
        oClient.SetIPAdress("169.254.41.185"); //eth
        //oClient.SetIPAdress("192.168.178.60"); //wlan
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        ThreadMXBean newBean = ManagementFactory.getThreadMXBean();
        try
        {
            if (newBean.isThreadCpuTimeSupported())
                newBean.setThreadCpuTimeEnabled(true);
            else
                throw new AccessControlException("");
        }
        catch (AccessControlException e)
        {
            System.out.println("CPU Usage monitoring is not available!");
            System.exit(0);
        }
        try {
            final long starttime = System.nanoTime();
            double avg=0.0;
            long lastTime = System.nanoTime();
            long lastThreadTime = newBean.getCurrentThreadCpuTime();

            float smoothLoad = 0;
            for(int i = 0; i< 10000; i++){
                //System.out.println(operatingSystemMXBean.getSystemCpuLoad());
                //avg =(operatingSystemMXBean.getProcessCpuLoad()+ i * avg )/(i+1);
                oClient.SendStringCreateNewConnection("hallo pi");
                // Calculate coarse CPU usage:
                long time = System.nanoTime();
                long threadTime = newBean.getCurrentThreadCpuTime();
                double load = (threadTime - lastThreadTime) / (double)(time - lastTime);
                // Smooth it.
                smoothLoad += (load - smoothLoad) * 0.1; // damping factor, lower means less responsive, 1 means no smoothing.

                // For next iteration.
                lastTime = time;
                lastThreadTime = threadTime;
            }
            final long endtime = System.nanoTime();
            System.out.println("Whole time is:");
            System.out.println(endtime-starttime);
            System.out.println("Avarage process load:");
            System.out.println(smoothLoad);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void UseServer()
    {
        BaseServer oServer;
        if(m_nProtocol == 0)
        {
            oServer = new TCPServer();

        }
        else if(m_nProtocol == 1)
        {
            oServer = new UDPServer();
        }
        else
        {
            oServer = new MQTTServer();
        }


        oServer.SetPort(6300);
        try {
            oServer.ListentoPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void printUsage()
    {

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        System.out.println( "System Load: "+operatingSystemMXBean.getProcessCpuTime() );

    }
}
package RoC.NetworkProtocolsBench;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;


public class MonitoringThread extends Thread {
    private boolean stopped;
    private boolean bNotRead = false;

    private Map<Long, ThreadTime> threadTimeMap = new HashMap<Long, ThreadTime>();
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private OperatingSystemMXBean opBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private long _startTime = 0l;
    public MonitoringThread() {
        setName("MonitoringThread");
        _startTime = BenchNetworkTime.GetCurrentTime();
        start();
    }



    @Override
    public void run() {
        while(!stopped) {
            while(bNotRead) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            bNotRead = true;
            Set<Long> mappedIds;
            synchronized (threadTimeMap) {
                mappedIds = new HashSet<Long>(threadTimeMap.keySet());
            }

            long[] allThreadIds = threadBean.getAllThreadIds();

            removeDeadThreads(mappedIds, allThreadIds);

            mapNewThreads(allThreadIds);

            Collection<ThreadTime> values;
            synchronized (threadTimeMap) {
                values = new HashSet<ThreadTime>(threadTimeMap.values());
            }

            for (ThreadTime threadTime : values) {
                synchronized (threadTime) {
                    threadTime.setCurrent(threadBean.getThreadCpuTime(threadTime.getId()));
                }
            }

            for (ThreadTime threadTime : values) {
                synchronized (threadTime) {
                    threadTime.setLast(threadTime.getCurrent());
                }
            }
        }
    }

    private void mapNewThreads(long[] allThreadIds) {
        for (long id : allThreadIds) {
            synchronized (threadTimeMap) {
                if(!threadTimeMap.containsKey(id))
                    threadTimeMap.put(id, new ThreadTime(id));
            }
        }
    }

    private void removeDeadThreads(Set<Long> mappedIds, long[] allThreadIds) {
        outer: for (long id1 : mappedIds) {
            for (long id2 : allThreadIds) {
                if(id1 == id2)
                    continue outer;
            }
            synchronized (threadTimeMap) {
                threadTimeMap.remove(id1);
            }
        }
    }

    public void stopMonitor() {
        this.stopped = true;
    }
    //Total load by thread t
    public double getUsageByThread(Thread t) {
        ThreadTime info;
        synchronized (threadTimeMap) {
            info = threadTimeMap.get(t.getId());
        }

        double usage = 0D;
        if(info != null) {
            synchronized (info) {
                usage = (threadBean.getThreadCpuTime(info.getId()) - info.getLast()) / ((BenchNetworkTime.GetCurrentTime() - _startTime)* 10000D);
                bNotRead = false;
            }
        }
        return usage;
    }

    static class ThreadTime {

        private long id;
        private long last;
        private long current;

        public ThreadTime(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public long getLast() {
            return last;
        }

        public void setLast(long last) {
            this.last = last;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(long current) {
            this.current = current;
        }
    }
}

class PerformanceMonitor {

    double cpuTime =0D;
    double uptime = 0D;
    int nCount = 0;
    double dAvgCore = 0D;
    double dTotalAvg = 0D;
    SystemInfo si;
    HardwareAbstractionLayer hal ;
    OperatingSystem os ;
    OSProcess p ;
    CentralProcessor cP;
    long[] loadTicks;
    long[][] oldTotalTicks;
    Semaphore m_oSemaphore;
    HashMap<String, Long> m_aNetRecievedBytes;

    PerformanceMonitor(Semaphore oSemo)
    {
        m_oSemaphore = oSemo;
        m_aNetRecievedBytes = new HashMap<>();
        while(true) {
            try {
                if (!! m_oSemaphore.tryAcquire(0, TimeUnit.SECONDS)) break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;
        }
        si = new SystemInfo();
         hal = si.getHardware();
         os = si.getOperatingSystem();
         p = os.getProcess(os.getProcessId());
         cP = hal.getProcessor();
         loadTicks = cP.getSystemCpuLoadTicks();
         oldTotalTicks = cP.getProcessorCpuLoadTicks();
        uptime = p.getUpTime();
        cpuTime = p.getKernelTime() + p.getUserTime();
         m_oSemaphore.release();
    }

    public void Next()
    {
        while(true) {
            try {
                if (!!m_oSemaphore.tryAcquire(0, TimeUnit.SECONDS)) break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;
        }
        si = new SystemInfo();
        hal = si.getHardware();
        os = si.getOperatingSystem();
        p = os.getProcess(os.getProcessId());
        cP = hal.getProcessor();

        dAvgCore = (dAvgCore * nCount + getCoreLoad()) / (nCount + 1);
        dTotalAvg = (dTotalAvg * nCount + getTotalLoad()) / (nCount + 1);
        updateNetwork();
        nCount++;
        m_oSemaphore.release();
    }

    public double GetAverageCoreLoad()
    {
        return dAvgCore;
    }

    public double GetTotalUsage()
    {
        return dTotalAvg;
    }

    public  HashMap<String, Long> GetNetworkInfo () { return  m_aNetRecievedBytes;}


    private double getCoreLoad() {

        double oldUpTime = uptime;
        double oldcpuTime = cpuTime;

        uptime = p.getUpTime();
        cpuTime = p.getKernelTime() + p.getUserTime();
        return (cpuTime-oldcpuTime) / (uptime - oldUpTime);
    }


    private double getTotalLoad()
    {
        double[] dLoad = cP.getProcessorCpuLoadBetweenTicks(oldTotalTicks);
        oldTotalTicks = cP.getProcessorCpuLoadTicks();
        double sum = 0;
        for (int i = 0; i < dLoad.length; i++) {
            sum += dLoad[i];
        }
        return sum / dLoad.length;
    }

    private void updateNetwork() {
        NetworkIF[] ni = hal.getNetworkIFs();
        for (int nIndex = 0; nIndex < ni.length; nIndex++) {
            Long nPre = m_aNetRecievedBytes.get(ni[nIndex].getDisplayName());
            if (nPre == null)
                nPre = Long.valueOf(0);

            m_aNetRecievedBytes.put(ni[nIndex].getDisplayName(), (ni[nIndex].getBytesRecv() + nPre));
        }
    }
}
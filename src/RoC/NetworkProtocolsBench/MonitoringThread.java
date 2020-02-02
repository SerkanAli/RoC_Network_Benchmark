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
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;


public class MonitoringThread extends Thread {

    private long refreshInterval;
    private boolean stopped;

    private Map<Long, ThreadTime> threadTimeMap = new HashMap<Long, ThreadTime>();
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private OperatingSystemMXBean opBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public MonitoringThread(long refreshInterval) {
        this.refreshInterval = refreshInterval;

        setName("MonitoringThread");

        start();
    }

    @Override
    public void run() {
        while(!stopped) {
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

            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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

    //Total load by all the threads in JVM
    public double getTotalUsage() {
        Collection<ThreadTime> values;
        synchronized (threadTimeMap) {
            values = new HashSet<ThreadTime>(threadTimeMap.values());
        }

        double usage = 0D;
        for (ThreadTime threadTime : values) {
            synchronized (threadTime) {
                usage += (threadTime.getCurrent() - threadTime.getLast()) / (refreshInterval * 10000);
            }
        }
        return usage;
    }

    // Avarage load per CPU (core)
    public double getAvarageUsagePerCPU() {
        return getTotalUsage() / opBean.getAvailableProcessors();
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
                usage = (info.getCurrent() - info.getLast()) / (refreshInterval * 10000);
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
    double dAvgThread = 0D;
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

    PerformanceMonitor(Semaphore oSemo)
    {
        m_oSemaphore = oSemo;
        while(! m_oSemaphore.tryAcquire());
        si = new SystemInfo();
         hal = si.getHardware();
         os = si.getOperatingSystem();
         p = os.getProcess(os.getProcessId());
         cP = hal.getProcessor();
         loadTicks = cP.getSystemCpuLoadTicks();
         oldTotalTicks = cP.getProcessorCpuLoadTicks();
         m_oSemaphore.release();
    }

    public void Next()
    {
        while(!m_oSemaphore.tryAcquire());
        si = new SystemInfo();
        hal = si.getHardware();
        os = si.getOperatingSystem();
        p = os.getProcess(os.getProcessId());
        cP = hal.getProcessor();

        dAvgThread = (dAvgThread * nCount + getProcessRecentCpuUsage()) / (nCount + 1);
        dAvgCore = (dAvgCore * nCount + getCPULoad()) / (nCount + 1);
        dTotalAvg = (dTotalAvg * nCount + getTotalLoad()) / (nCount + 1);
        nCount++;
        m_oSemaphore.release();
    }

    public double GetAvarageThreadUsage()
    {
        return dAvgThread;
    }
    public double GetAvarageCoreusage()
    {
        return dAvgCore;
    }

    public double GetTotalUsage()
    {
        return dTotalAvg;
    }

    //public double Get

    private double getProcessRecentCpuUsage() {
        double output = 0d;


        if (cpuTime != 0) {
            double uptimeDiff = p.getUpTime() - uptime;
            double cpuDiff = (p.getKernelTime() + p.getUserTime()) - cpuTime;
            output = cpuDiff / uptimeDiff;
        } else {
            output = ((double) (p.getKernelTime() + p.getUserTime())) / (double) p.getUserTime();
        }

        // Record for next invocation
        uptime = p.getUpTime();
        cpuTime = p.getKernelTime() + p.getUserTime();
        return output / hal.getProcessor().getLogicalProcessorCount();
    }

    private double getCPULoad(){
        double dLoad = cP.getSystemCpuLoadBetweenTicks(loadTicks);
        loadTicks = cP.getSystemCpuLoadTicks();
        return dLoad;
    }

    private double getTotalLoad()
    {
        double[] dLoad = cP.getProcessorCpuLoadBetweenTicks(oldTotalTicks);
        oldTotalTicks = cP.getProcessorCpuLoadTicks();
        double sum = 0;
        int divider = dLoad.length;
        for (int i = 0; i < dLoad.length; i++) {
            //sum = Math.max(sum, dLoad[i]);
            sum += dLoad[i];
            if(dLoad[i] == 0d)
                divider--;
        }
        return sum / divider;
    }
}
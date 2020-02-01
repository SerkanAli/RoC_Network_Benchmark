package RoC.NetworkProtocolsBench;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import static sun.management.ManagementFactoryHelper.getOperatingSystemMXBean;

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
    private int  availableProcessors = getOperatingSystemMXBean().getAvailableProcessors();
    private long lastSystemTime      = 0;
    private long lastProcessCpuTime  = 0;

    public synchronized double getCpuUsage()
    {
        if ( lastSystemTime == 0 )
        {
            baselineCounters();
            return 0D;
        }

        long systemTime     = System.nanoTime();
        long processCpuTime = 0;

        if ( getOperatingSystemMXBean() instanceof OperatingSystemMXBean )
        {
            processCpuTime = ( (OperatingSystemMXBean) getOperatingSystemMXBean() ).getProcessCpuTime();
        }

        double cpuUsage = (double) ( processCpuTime - lastProcessCpuTime ) / ( systemTime - lastSystemTime );

        lastSystemTime     = systemTime;
        lastProcessCpuTime = processCpuTime;

        return cpuUsage / availableProcessors;
    }

    private void baselineCounters()
    {
        lastSystemTime = System.nanoTime();

        if ( getOperatingSystemMXBean() instanceof OperatingSystemMXBean )
        {
            lastProcessCpuTime = ( (OperatingSystemMXBean) getOperatingSystemMXBean() ).getProcessCpuTime();
        }
    }
}
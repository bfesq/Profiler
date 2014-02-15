package com.greenit.profiler.service;


import com.greenit.profiler.configuration.ProfilerProperties;
import com.greenit.profiler.statistics.SummaryMetrics;
import com.greenit.profiler.writer.DailyLogWriterImpl;
import com.greenit.profiler.writer.LogWriter;
import com.greenit.profiler.writer.SystemLogWriter;
import com.mentorgen.profile.runtime.Frame;
import com.mentorgen.profile.runtime.FrameKey;
import com.mentorgen.profile.runtime.Profile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsService implements Runnable {
    private final ProfilerProperties profilerProperties;
    private AtomicInteger dataCount = new AtomicInteger(0);
    private AtomicInteger stackDataCount = new AtomicInteger(0);
    private ConcurrentMap<String, SummaryMetrics> data = new ConcurrentHashMap<String, SummaryMetrics>(200);
    private ConcurrentMap<FrameKey, SummaryMetrics> stackData = new ConcurrentHashMap<FrameKey, SummaryMetrics>(200);
    private boolean run = true;
    private Object lock = new Object();
    private LogWriter logWriter;
    private BigInteger sumOverhead = BigInteger.ZERO;
    private PubishService<String> publishService = null;
    private PubishService<FrameKey> publishStackService = null;
    private boolean nanoPrecision;
    private Timer timer;
    
    public StatisticsService(ProfilerProperties profilerProperties) throws IOException {
        this.profilerProperties = profilerProperties;
        nanoPrecision = profilerProperties.isNanoPrecision();
        long start = nanoPrecision ? System.nanoTime() : System.currentTimeMillis();
        if (profilerProperties.getOutputFile() != null && !(profilerProperties.getOutputFile().trim().length() == 0)) {
            this.logWriter = new DailyLogWriterImpl(profilerProperties.getOutputFile(),".csv");
        } else {
            this.logWriter = new SystemLogWriter();
        }
        publishService = new PubishService<String>();
        publishStackService = new PubishService<FrameKey>();
        // spawn a daemon timer thread to publish the summary metrics
        startPublishingThread();
        this.addOverhead(nanoPrecision ? System.nanoTime() - start : System.currentTimeMillis() - start);
    }

    private void startPublishingThread() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                publish();
            }
        }, profilerProperties.getInitialDelayOutput()*1000,
           profilerProperties.getFrequencyOfOutput()*1000);
    }
    public void stop() {
        this.run = false;
        timer.cancel();
    }

    public boolean isRun() {
        return run;
    }

    public void run() {
        try {
            while (isRun()) {
                if (Profile.queueForStatistics != null) {
                    Frame f = Profile.queueForStatistics.take();
                    long start = this.nanoPrecision ? System.nanoTime() : System.currentTimeMillis();
                    addOverhead(f.getMetrics().getTotalOverhead());
                    if (Profile.PROCESS_STACKTRACES) {
                        if (f.getMethodDescriptor() != null && f.getKey() != null) {
                            SummaryMetrics sm = stackData.get(f.getKey());
                            if (sm == null && stackDataCount.intValue() < profilerProperties.getMaxStackDataCount()) {
                                sm = new SummaryMetrics();
                                stackData.put(f.getKey(), sm);
                                stackDataCount.incrementAndGet();
                            }
                            if (sm != null) {
                               synchronized (lock) {
                                    sm.addEntry(f.getMetrics().getTotalTime(), f.getMetrics().getWaitTime(),
                                            f.getMetrics().isExceptionThrownByMethod());
                                }
                            }
                        }
                    }
                    if (f.getMethodDescriptor() != null) {
                        SummaryMetrics sm = data.get(f.getMethodDescriptor().toString());
                        if (sm == null && dataCount.intValue() < profilerProperties.getMaxMethodCount()) {
                            sm = new SummaryMetrics();
                            data.put(f.getMethodDescriptor().toString(), sm);
                            dataCount.incrementAndGet();
                        }
                        if (sm != null) {
                            synchronized (lock) {
                                sm.addEntry(f.getMetrics().getTotalTime(), f.getMetrics().getWaitTime(),
                                        f.getMetrics().isExceptionThrownByMethod());
                            }
                        }
                    } else {
                        System.out.println("Stopping profiler");
                        publish();
                        stop();
                    }
                    this.addOverhead(nanoPrecision ? System.nanoTime() - start : System.currentTimeMillis() - start);
                } else {
                    // sleep until trying profiler queue again
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Shutting down Timing Service");
            this.publish();
            return;
        }
    }

    /**
     * Can be called on a timer thread to publish CSV at regular intervals
     * May need to synchronise
     */
    public void publish() {
        synchronized (lock) {
            long start = this.nanoPrecision ? System.nanoTime() : System.currentTimeMillis();
            Date now = new Date();
            publishService.toCSV(data, this.sumOverhead.longValue(),logWriter, now);
            if (Profile.PROCESS_STACKTRACES) {
                publishStackService.toCSV(this.stackData, this.sumOverhead.longValue(), logWriter, now);
            }
            if (profilerProperties.isResetOnOutput()) {
                clearSummaryMetrics();
            }
            this.addOverhead(nanoPrecision ? System.nanoTime() - start : System.currentTimeMillis() - start);
        }
    }

    private void clearSummaryMetrics() {
        synchronized (lock) {
            long start = this.nanoPrecision ? System.nanoTime() : System.currentTimeMillis();
            for (String methodName : data.keySet()) {
                SummaryMetrics sm = data.get(methodName);
                sm.clear();
            }
            for (FrameKey key : stackData.keySet()) {
                SummaryMetrics sm = data.get(key);
                sm.clear();
            }
            this.addOverhead(nanoPrecision ? System.nanoTime() - start : System.currentTimeMillis() - start);
        }
    }

    private void addOverhead(long time) {
        this.sumOverhead = sumOverhead.add(BigInteger.valueOf(time));
    }


    
}

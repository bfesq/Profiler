package com.greenit.profiler.statistics;

import org.junit.*;

import com.greenit.profiler.statistics.SummaryMetrics;

import static junit.framework.Assert.*;

public class SummaryMetricsTest {
    
    @Test
    public void whenOneZeroEntryMeanStdDevCountCorrect() {
        SummaryMetrics sm = new SummaryMetrics();
        sm.addEntry(0, 0, false);
        assertEquals(1, sm.getCount());
        assertEquals(0.0,sm.runtimeMean());
        assertEquals(0.0,sm.runtimeStdDeviation());
        assertEquals(0,sm.runtimeMax());
        assertEquals(0,sm.runtimeMin());
        assertEquals(0.0,sm.waittimeMean());
        assertEquals(0.0,sm.waittimeStdDeviation());
    }


    @Test
    public void whenTenEntrieMeanStdDevCountCorrect() {
        SummaryMetrics sm = new SummaryMetrics();
        for (int i = 0; i < 10; i ++) {
            sm.addEntry(i, 0, false);
        }
        assertEquals(10, sm.getCount());
        assertEquals(4.5,sm.runtimeMean());
        assertEquals(2872281, Math.round(sm.runtimeStdDeviation()*1000000));
        assertEquals(9,sm.runtimeMax());
        assertEquals(0,sm.runtimeMin());
        assertEquals(0.0,sm.waittimeMean());
        assertEquals(0.0,sm.waittimeStdDeviation());
    }

    @Test
    public void whenTenEntrieWaittimeMeanStdDevCountCorrect() {
        SummaryMetrics sm = new SummaryMetrics();
        for (int i = 0; i < 10; i ++) {
            sm.addEntry(0, i , false);
        }
        assertEquals(10, sm.getCount());
        assertEquals(0.0,sm.runtimeMean());
        assertEquals(0,sm.runtimeMax());
        assertEquals(0,sm.runtimeMin());
        assertEquals(4.5,sm.waittimeMean());
        assertEquals(2872281, Math.round(sm.waittimeStdDeviation()*1000000));
    }

    @Test
    public void when100MillionEntriesNanoSecondsNoOverflow() {
        long TIME_MS = System.nanoTime();
        double D_TIME_MS = TIME_MS*1.0;
        long NUM_ENTRIES = 1*1000000;
        SummaryMetrics sm = new SummaryMetrics();
        long t1 = System.currentTimeMillis();
        int j = 0;
        for (int i = 0; i < NUM_ENTRIES; i ++) {
           sm.addEntry(TIME_MS , 0, false);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("100 million entries Time taken = " + (t2-t1) + "ms");
        assertEquals(NUM_ENTRIES, sm.getCount());
        assertEquals(D_TIME_MS,sm.runtimeMean());
        assertEquals(0.0, sm.runtimeStdDeviation()*1000000);
        assertEquals(TIME_MS,sm.runtimeMax());
        assertEquals(TIME_MS,sm.runtimeMin());
        assertEquals(0.0,sm.waittimeMean());
        assertEquals(0.0,sm.waittimeStdDeviation());
    }
}

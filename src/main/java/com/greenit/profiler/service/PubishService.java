package com.greenit.profiler.service;

import com.greenit.profiler.statistics.SummaryMetrics;
import com.greenit.profiler.writer.LogWriter;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: farrb
 * Date: 05/04/12
 * Time: 15:27
 */
public class PubishService<T> {
    final static String CSV_HEADINGS = "Date, Name, Count (excl exceptions), Runtime Mean, Runtime Min, Runtime Max, " +
            "Runtime Std Deviation, Wait time Mean, Wait time Std Deviation, Wait Count, Wait time Mean by Wait Count, " +
            "Wait time Std Dev by Wait Count, Exception Count, Runtime Mean for Exception Count";
    public String toCSV(ConcurrentMap<T, SummaryMetrics> data, Long sumOverhead, LogWriter logWriter, Date dateForLogWriter) {
        StringBuilder sb = new StringBuilder(500);
        Date now = new Date();
        logWriter.println(dateForLogWriter, "Total overheard :" + sumOverhead);
        logWriter.println(dateForLogWriter,CSV_HEADINGS);
        for (T methodName : data.keySet()) {
            SummaryMetrics sm = data.get(methodName);
            sb.delete(0, sb.length());
            sb.append(now).append(",")
                    .append(methodName.toString()).append(",")
                    .append(sm.getCount()).append(",")
                    .append(sm.runtimeMean()).append(",")
                    .append(sm.runtimeMin()).append(",")
                    .append(sm.runtimeMax()).append(",")
                    .append(sm.runtimeStdDeviation()).append(",")
                    .append(sm.waittimeMean()).append(",")
                    .append(sm.waittimeStdDeviation()).append(",")
                    .append(sm.getWaitCount()).append(",")
                    .append(sm.waittimeMeanForWaitCount()).append(",")
                    .append(sm.waittimeStdDeviationForWaitCount()).append(",")
                    .append(sm.getExceptionCount()).append(",")
                    .append(sm.exceptiontimeMean());
            logWriter.println(dateForLogWriter, sb.toString());
        }
        return sb.toString();
    }

}

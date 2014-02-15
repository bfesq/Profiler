package com.greenit.profiler.writer;

import java.util.Date;

public interface LogWriter {
    public static final String SEPERATOR = ",";
    public void println(String x);
    public void println(Date lineDate, String x);
    public void print(String s);
    public void println();
    public void close();
}

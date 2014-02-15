package com.greenit.profiler.writer;


import java.io.PrintWriter;
import java.util.Date;

public class SystemLogWriter extends PrintWriter implements LogWriter {

    public SystemLogWriter() {
        super(System.out);
    }

    public void println(Date lineDate, String line) {
        super.println(line);
        super.flush();
    }
}

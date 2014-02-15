package com.greenit.profiler.writer;

import com.greenit.profiler.util.StringFunction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DailyLogWriterImpl implements LogWriter {
    private static final SimpleDateFormat DATE_PATTERN_EXTENSION = new SimpleDateFormat("yyyy-MM-dd");
    private final String EXTENSION;
    private BufferedWriter fw = null;

    private int currentDayOfMonth;
    private String baseFileName;

    /**
     * For file output use something like this
     * //PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("foo.out")));
     */
    public DailyLogWriterImpl(String fileName, String extension) throws IOException {
        EXTENSION = extension == null ? ".log" : extension;
        this.baseFileName = fileName;
        Date now = new Date();
        currentDayOfMonth = getDayOfMonth(now);
        fw = new BufferedWriter(new FileWriter(new File(baseFileName + DATE_PATTERN_EXTENSION.format(now) + EXTENSION), true));
    }

    private int getDayOfMonth(Date date) {
        Calendar cnow = Calendar.getInstance();
        cnow.setTime(date);
        return cnow.get(Calendar.DAY_OF_MONTH);
    }

    public void close() {
        if (fw != null) {
            try {
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void rollover() {
        Date now = new Date();
        int dayOfMonth = getDayOfMonth(now);

        if (dayOfMonth != this.currentDayOfMonth) {
            close();
            try {
                fw = new BufferedWriter(new FileWriter(new File(baseFileName + DATE_PATTERN_EXTENSION.format(now) + EXTENSION),true));
            } catch (IOException e) {
                System.err.println("Failed to open new log file:" + baseFileName + DATE_PATTERN_EXTENSION.format(now)
                        + EXTENSION + e.getMessage());
                e.printStackTrace(System.err);
            }
            currentDayOfMonth = dayOfMonth;
        }
    }

    public void println(Date d, String x) {
        int dayOfMonth = getDayOfMonth(d);
        if (dayOfMonth != this.currentDayOfMonth) {
            rollover();
        }
        println(x);
    }
    public void println(String x) {
        try {
            fw.write(x);
            fw.write(StringFunction.NEW_LINE);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);  //To change body of catch statement use File | Settings | File Templates.
        }
        rollover();
    }

    public void print(String s) {
        try {
            fw.write(s);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void println() {
        try {
            fw.write(StringFunction.NEW_LINE);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        rollover();
    }

}
                                      
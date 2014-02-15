package com.greenit.profiler.configuration;


import java.util.regex.Pattern;

public class ClassMethodRegex {
    private Pattern classRegex;
    private Pattern methodRegex;
    private long minimumThreshold;
    private boolean threshold = true;

    public Pattern getClassRegex() {
        return classRegex;
    }

    public void setClassRegex(String classRegex) {
        this.classRegex = Pattern.compile(classRegex);
    }

    public Pattern getMethodRegex() {
        return methodRegex;
    }

    public void setMethodRegex(String methodRegex) {
        this.methodRegex = Pattern.compile(methodRegex);
    }

    public long getMinimumThreshold() {
        return minimumThreshold;
    }

    public void setMinimumThreshold(long minimumThreshold) {
        this.minimumThreshold = minimumThreshold;
        this.threshold = this.minimumThreshold > 0;
    }

    public boolean isThreshold() {
        return threshold;
    }
}

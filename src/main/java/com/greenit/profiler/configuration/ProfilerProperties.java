package com.greenit.profiler.configuration;

import com.greenit.profiler.util.StringFunction;
import com.mentorgen.tools.profile.instrument.ClassMethodFilter;

import java.util.ArrayList;
import java.util.List;

public class ProfilerProperties implements ClassMethodFilter {
    private List<ClassMethodRegex> includeMatches = new ArrayList<ClassMethodRegex>();
    private List<ClassMethodRegex> excludeMatches = new ArrayList<ClassMethodRegex>();
    private int maxStackDataCount = 25000;
    private int maxMethodCount = 20000;
    private boolean includeClassInit = false;
    private boolean includeInit = false;
    // Time in seconds of output
    private Long frequencyOfOutput = 0L;
    private Long initialDelayOutput = 10L;
    private boolean resetOnOutput = false;
    private Long defaultMinThreshold = 0L;


    // Open and appends to file then closes
    private String outputFile;
    // Millisecond MS or Nanosecond NS precision
    private String precision;

    public List<ClassMethodRegex> getIncludeMatches() {
        return includeMatches;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public List<ClassMethodRegex> getExcludeMatches() {
        return excludeMatches;
    }

    public boolean isIncludeClassInit() {
        return includeClassInit;
    }

    public void setIncludeClassInit(boolean includeClassInit) {
        this.includeClassInit = includeClassInit;
    }

    public boolean isIncludeInit() {
        return includeInit;
    }

    public void setIncludeInit(boolean includeInit) {
        this.includeInit = includeInit;
    }

    public String getPrecision() {
        return precision;
    }
    public boolean isNanoPrecision() {
        if ("NS".equalsIgnoreCase(precision)) {
            return true;
        }
        return false;
    }
    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public Long getFrequencyOfOutput() {
        return frequencyOfOutput;
    }

    public void setFrequencyOfOutput(Long frequencyOfOutput) {
        this.frequencyOfOutput = frequencyOfOutput;
    }

    public Long getInitialDelayOutput() {
        return initialDelayOutput;
    }

    public void setInitialDelayOutput(Long initialDelayOutput) {
        this.initialDelayOutput = initialDelayOutput;
    }

    public boolean isResetOnOutput() {
        return resetOnOutput;
    }

    public void setResetOnOutput(boolean resetOnOutput) {
        this.resetOnOutput = resetOnOutput;
    }

    public boolean profileClass(String className) {
        boolean result = false;
        String convertedClassName = StringFunction.convertClassName(className);
        if (!convertedClassName.startsWith("com.greenit.profiler") &&
        		!convertedClassName.startsWith("com.mentorgen.profile")) {
            //No chance of profiling profiler!
            result = isIncluded(convertedClassName) &&
                    !isExcluded(convertedClassName);
        }
        /*
        if (result) {
            System.out.println("******************** profiling:" + className);
        } else {
            System.out.println("******************** NOT profiling:" + className);
        }
        */
        return result;
    }

    public boolean profileMethod(String className, String method) {
        boolean result = false;
        if (profileClass(className)) {
            if ('<' == method.charAt(0)) {
                if (method.startsWith("<clinit>")) {
                    result = includeClassInit;
                } else if (method.startsWith("<init>")) {
                    result = includeInit;
                }  else {
                    System.err.println("Not profiling :" + className + ":" + method);
                }
            } else {
            	// could save this conversion
                String convertedClassName = StringFunction.convertClassName(className);
                result = isIncluded(convertedClassName,method, -1) &&
                        !isExcluded(convertedClassName,method);
            }
        }

        return result;
    }

    public boolean record(String className, String method, long runtime) {
        return isIncluded(className, method, runtime);
    }

    private boolean isIncluded(String className) {
        boolean result = false;
        for (ClassMethodRegex classMethodRegex : includeMatches) {
            if (classMethodRegex.getClassRegex() != null &&
                classMethodRegex.getClassRegex().matcher(className).find()) {
                result = true;
                break;
            }
        }
        return result;
    }


    private boolean isIncluded(String className, String method, long runtime) {
        for (ClassMethodRegex classMethodRegex : includeMatches) {
            if (classMethodRegex.getClassRegex() != null &&
                classMethodRegex.getClassRegex().matcher(className).find()) {
                if (classMethodRegex.getMethodRegex() != null &&
                    classMethodRegex.getMethodRegex().matcher(method).find()) {
                    if (runtime != -1 && classMethodRegex.isThreshold()) {
                        if (runtime >= classMethodRegex.getMinimumThreshold()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isExcluded(String className, String method) {
        for (ClassMethodRegex classMethodRegex : excludeMatches) {
            if (classMethodRegex.getClassRegex() != null &&
                classMethodRegex.getClassRegex().matcher(className).find()) {
                if (classMethodRegex.getMethodRegex() != null &&
                    classMethodRegex.getMethodRegex().matcher(method).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExcluded(String className) {
        boolean result = false;
        for (ClassMethodRegex classMethodRegex : excludeMatches) {
            if (classMethodRegex.getClassRegex() != null &&
                classMethodRegex.getClassRegex().matcher(className).find() &&
                classMethodRegex.getMethodRegex().pattern().equals(".*")) {
                result = true;
                break;
            }
        }
        return result;
    }

    public int getMaxStackDataCount() {
        return maxStackDataCount;
    }

    public void setMaxStackDataCount(int maxStackDataCount) {
        this.maxStackDataCount = maxStackDataCount;
    }

    public int getMaxMethodCount() {
        return maxMethodCount;
    }

    public void setMaxMethodCount(int maxMethodCount) {
        this.maxMethodCount = maxMethodCount;
    }

    public Long getDefaultMinThreshold() {
        return defaultMinThreshold;
    }

    public void setDefaultMinThreshold(Long defaultMinThreshold) {
        this.defaultMinThreshold = defaultMinThreshold;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProfilerProperties [includeMatches=");
		builder.append(includeMatches);
		builder.append(", excludeMatches=");
		builder.append(excludeMatches);
		builder.append(", maxStackDataCount=");
		builder.append(maxStackDataCount);
		builder.append(", maxMethodCount=");
		builder.append(maxMethodCount);
		builder.append(", includeClassInit=");
		builder.append(includeClassInit);
		builder.append(", includeInit=");
		builder.append(includeInit);
		builder.append(", frequencyOfOutput=");
		builder.append(frequencyOfOutput);
		builder.append(", initialDelayOutput=");
		builder.append(initialDelayOutput);
		builder.append(", resetOnOutput=");
		builder.append(resetOnOutput);
		builder.append(", defaultMinThreshold=");
		builder.append(defaultMinThreshold);
		builder.append(", outputFile=");
		builder.append(outputFile);
		builder.append(", precision=");
		builder.append(precision);
		builder.append("]");
		return builder.toString();
	}

	

    
}


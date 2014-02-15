package com.greenit.profiler.util;

public class StringFunction {
    public static final String NEW_LINE = String.format("%n");
    /**
     * Converts a className that contains '/' into '.'
     * @param className
     */
    public static String convertClassName(String className) {
        assert className != null;
        StringBuilder b = new StringBuilder(className.replace('/', '.'));
        return b.toString();
    }

    public static String getShortName(String className) {
        assert className != null;
        int index = className.lastIndexOf(".");
        String shortName = null;
        String packageName = "";

        if (index > -1) {
            shortName = className.substring(index + 1);
        } else {
            shortName = className;
        }
        return shortName;
    }

    public static String getPackageName(String className) {
        assert className != null;
        int index = className.lastIndexOf(".");
        String packageName = "";

        if (index > -1) {
            packageName = className.substring(0, index);
        }
        return packageName;
    }
}


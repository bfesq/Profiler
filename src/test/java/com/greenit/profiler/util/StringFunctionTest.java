package com.greenit.profiler.util;

import org.junit.*;

import com.greenit.profiler.util.StringFunction;

import static junit.framework.Assert.assertEquals;

public class StringFunctionTest {
    @Test
    public void testConvertClassName() {
        assertEquals("com.greenit.abc", StringFunction.convertClassName("com/greenit/abc"));
        assertEquals("com.greenit.abc$innerclass", StringFunction.convertClassName("com/greenit/abc$innerclass"));
    }
    @Test
    public void testPackageName() {
        assertEquals("com.greenit", StringFunction.getPackageName("com.greenit.abc"));
    }
    @Test
    public void testShortName() {
        assertEquals("abc", StringFunction.getShortName("com.greenit.abc"));
        assertEquals("abc", StringFunction.getShortName("abc"));
    }
}

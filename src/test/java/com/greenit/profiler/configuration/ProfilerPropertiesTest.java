package com.greenit.profiler.configuration;

import org.junit.*;

import com.greenit.profiler.configuration.ClassMethodRegex;
import com.greenit.profiler.configuration.ProfilerProperties;

import static org.junit.Assert.assertEquals;

public class ProfilerPropertiesTest {

    @Test
    public void testProfileClass() {
        ProfilerProperties props = new ProfilerProperties();
        ClassMethodRegex regex = new ClassMethodRegex();
        regex.setClassRegex("com[.]greenit[.].*");
        regex.setMethodRegex("count");
        props.getIncludeMatches().add(regex);
        assertEquals(true, props.profileClass("com.greenit.abc"));
        assertEquals(false, props.profileClass("comgreenit.abc"));
        assertEquals(false, props.profileClass("com.greenit2.abc"));
    }

    @Test
    public void testProfileMethod() {
        ProfilerProperties props = new ProfilerProperties();
    }
}

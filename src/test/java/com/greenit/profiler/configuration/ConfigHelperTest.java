package com.greenit.profiler.configuration;

import java.io.*;
import javax.xml.parsers.*;
import org.junit.*;
import org.xml.sax.*;

import com.greenit.profiler.configuration.ConfigHelper;
import com.greenit.profiler.configuration.ProfilerProperties;

import static org.junit.Assert.*;

public class ConfigHelperTest {

    @Test
    public void whenValidXMLFileProfilePropertiesProcessedCorrectly() {
        String XML = "<profiler-config>\n" +
                "    <csv-results>c:/tmp/profiler/out.csv</csv-results>\n" +
                "    <frequency-output-seconds>30</frequency-output-seconds>\n" +
                "<default-min-threshold>3000</default-min-threshold>\n" +
                "    <maxStackDataCount>100000</maxStackDataCount>\n" +
                "    <maxMethodCount>50000</maxMethodCount>\n" +
                "    <includeClassInit>true</includeClassInit>\n" +
                "    <includeInit>true</includeInit>\n" +
                "    <include>\n" +
                "        <class name=\"TestProfile\" method=\"[9]\" />\n" +
                "    </include>\n" +
                "    <exclude>\n" +
                "        <class name=\"TestProfile2\" method=\"main\" />\n" +
                "    </exclude>\n" +
                "</profiler-config>";
        InputStream is = new ByteArrayInputStream(XML.getBytes());

        ProfilerProperties props = null;
        try {
            props = ConfigHelper.readConfig(is);
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals("c:/tmp/profiler/out.csv", props.getOutputFile());
        assertEquals(Long.valueOf(30L), props.getFrequencyOfOutput());
        assertTrue(props.isIncludeClassInit());
        assertTrue(props.isIncludeInit());
        assertEquals(100000, props.getMaxStackDataCount());
        assertEquals(50000, props.getMaxMethodCount());
        assertEquals(1, props.getExcludeMatches().size());
        assertEquals(1, props.getIncludeMatches().size());

        assertEquals("TestProfile2", props.getExcludeMatches().get(0).getClassRegex().pattern());
        assertEquals("main", props.getExcludeMatches().get(0).getMethodRegex().pattern());
        assertEquals("TestProfile", props.getIncludeMatches().get(0).getClassRegex().pattern());
        assertEquals("[9]", props.getIncludeMatches().get(0).getMethodRegex().pattern());

    }

}

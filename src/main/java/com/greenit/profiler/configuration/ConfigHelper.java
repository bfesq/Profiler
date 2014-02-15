package com.greenit.profiler.configuration;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Very simple parsing of xml config
 */
public class ConfigHelper {
    static final String CSVRESULTS = "csv-results";
    static final String FREQUENCY = "frequency-output-seconds";
    static final String INCLUDE = "include";
    static final String EXCLUDE = "exclude";
    static final String CLASS = "class";
    static final String NAME = "name";
    static final String METHOD = "method";
    static final String PRECISION = "precision";
    static final String DEFAULT_MIN_THRESHOLD = "default-min-threshold";
    static final String MIN_THRESHOLD = "min-threshold";
    static final String MAX_STACKDATA = "maxStackDataCount";
    static final String MAX_METHODDATA  = "maxMethodCount";
    static final String INCLUDE_CLASSINIT  = "includeClassInit";
    static final String INCLUDE_INIT   = "includeInit";
    static final String INITIAL_DELAY_OUTPUT = "initialDelayOutput";
    static final String RESET_ON_OUTPUT = "resetOnOutput";

    public static File getConfigFileName(String args) {
        //"agentargs=config=c:/1.config"
        String filename = null;
        if (args != null && args.indexOf("config") > -1) {
            String[] keyvalue = args.split("=");
            for (int i = 0; i < keyvalue.length; i ++) {
                if ("config".equals(keyvalue[i])) {
                    if (i + 1 < keyvalue.length)  {
                        filename = keyvalue[i+1];
                    }
                }
            }
        }
        if (filename != null) {
            File f = new File(filename);
            if (f.exists() && f.canRead()) {
                return f;
            } else {
                System.err.println("Cannot find configuration file:" + filename + " exiting");
                System.exit(1);
            }
        }
        return null;
    }
    /*         JDK 1.6
    public static ProfilerProperties readConfig(InputStream in) throws XMLStreamException {
        ProfilerProperties properties = new ProfilerProperties();
        try {
            // First create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            String inclexcl = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have a item element we create a new item
                    if (CSVRESULTS.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        event = eventReader.nextEvent();
                        properties.setOutputFile(event.asCharacters().getData());
                    }
                    if (FREQUENCY.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        event = eventReader.nextEvent();
                        properties.setFrequencyOfOutput(Long.parseLong(event.asCharacters().getData()));
                    }
                    if (PRECISION.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        event = eventReader.nextEvent();
                        properties.setPrecision("NS".equals(event.asCharacters().getData()) ? "NS" : "MS");
                    }
                    if (INCLUDE.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        inclexcl = INCLUDE;
                    }
                    if (EXCLUDE.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        inclexcl = EXCLUDE;
                    }
                    if (CLASS.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        ClassMethodRegex regex = new ClassMethodRegex();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(NAME)) {
                                regex.setClassRegex(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals(METHOD)) {
                                regex.setMethodRegex(attribute.getValue());
                            }
                        }
                        if (INCLUDE.equals(inclexcl)) {
                            properties.getIncludeMatches().add(regex); 
                        } else if (EXCLUDE.equals(inclexcl)) {
                            properties.getExcludeMatches().add(regex);
                        }
                    }
                }           
                            
                            
            }
        } catch (XMLStreamException e) {
            throw e;
        }
        return properties;
    }
    */
    public static ProfilerProperties readConfig(InputStream in) throws SAXException, ParserConfigurationException, IOException {
        ProfilerProperties properties = new ProfilerProperties();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(in);
            Element docEle = dom.getDocumentElement();
            if (getTextValue(docEle,MAX_STACKDATA) != null) {
                properties.setMaxStackDataCount(Integer.parseInt(getTextValue(docEle,MAX_STACKDATA)));
            }
            if (getTextValue(docEle,MAX_METHODDATA) != null) {
                properties.setMaxMethodCount(Integer.parseInt(getTextValue(docEle,MAX_METHODDATA)));
            }
            properties.setIncludeClassInit("true".equals(getTextValue(docEle,INCLUDE_CLASSINIT)));
            properties.setIncludeInit("true".equals(getTextValue(docEle,INCLUDE_INIT)));
            properties.setResetOnOutput("true".equals(getTextValue(docEle,RESET_ON_OUTPUT)));
            if (getTextValue(docEle,INITIAL_DELAY_OUTPUT) != null) {
                properties.setInitialDelayOutput(Long.parseLong(getTextValue(docEle,INITIAL_DELAY_OUTPUT)));
            }
            properties.setOutputFile(getTextValue(docEle,CSVRESULTS));
            if (getTextValue(docEle,FREQUENCY) != null) {
                properties.setFrequencyOfOutput(Long.parseLong(getTextValue(docEle,FREQUENCY)));
            }
            properties.setPrecision("NS".equals(getTextValue(docEle,PRECISION)) ? "NS" : "MS");
            String dmt = getTextValue(docEle,DEFAULT_MIN_THRESHOLD);
            if (dmt != null && dmt.trim().length() > 0) {
                properties.setDefaultMinThreshold(Long.parseLong(dmt));
            }
            NodeList nl = docEle.getElementsByTagName(INCLUDE);
            if(nl != null && nl.getLength() > 0) {
                for(int i = 0 ; i < nl.getLength();i++) {
                    Element el = (Element)nl.item(i);
                    NodeList cl = el.getElementsByTagName(CLASS);
                    if (cl != null && cl.getLength() > 0) {
                        for(int j = 0 ; j < cl.getLength();j++) {
                            ClassMethodRegex regex = new ClassMethodRegex();
                            Element cel = (Element)cl.item(j);
                            regex.setClassRegex(cel.getAttribute(NAME));
                            regex.setMethodRegex(cel.getAttribute(METHOD));
                            String minThreshold = cel.getAttribute(MIN_THRESHOLD);
                            if (minThreshold != null && minThreshold.trim().length() > 0) {
                                regex.setMinimumThreshold(Long.parseLong(minThreshold));
                            } else if (properties.getDefaultMinThreshold() != null) {
                                regex.setMinimumThreshold(properties.getDefaultMinThreshold());    
                            }
                            properties.getIncludeMatches().add(regex);
                        }
                    }
                }
            }
            nl = docEle.getElementsByTagName(EXCLUDE);
            if(nl != null && nl.getLength() > 0) {
                for(int i = 0 ; i < nl.getLength();i++) {
                    Element el = (Element)nl.item(i);
                    NodeList cl = el.getElementsByTagName(CLASS);
                    if (cl != null && cl.getLength() > 0) {
                        for(int j = 0 ; j < cl.getLength();j++) {
                            ClassMethodRegex regex = new ClassMethodRegex();
                            Element cel = (Element)cl.item(j);
                            regex.setClassRegex(cel.getAttribute(NAME));
                            regex.setMethodRegex(cel.getAttribute(METHOD));
                            properties.getExcludeMatches().add(regex);
                        }
                    }
                }
            }


        return properties;
    }

    private static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

}

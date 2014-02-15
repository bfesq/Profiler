package com.greenit.profiler;

import com.greenit.profiler.configuration.ConfigHelper;
import com.greenit.profiler.configuration.ProfilerProperties;
import com.greenit.profiler.service.StatisticsService;
import com.mentorgen.profile.runtime.Frame;
import com.mentorgen.profile.runtime.Profile;
import com.mentorgen.tools.profile.instrument.Transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;


public class Main {
    public static Thread statisticsService;

    /**
     * Run with -javaagent:C:\Statistical-Profiler-0.0.1-SNAPSHOT.jar=agentargs=profiler.config:C:/project/StatisticalProfiler/src/test/resources/profiler.config
     * @param args
     * @param instrumentation
     */
    public static void premain(String args, Instrumentation instrumentation) {
        // read in properties file here for class names
        ProfilerProperties properties = null;
        File config = ConfigHelper.getConfigFileName(args);

        if (config != null) {
            try {
            	if (!config.exists()) {
            		System.err.println("Could not locate config file:" + config.getAbsolutePath());
            		System.exit(1);
            	}
                properties = ConfigHelper.readConfig(new FileInputStream(config));
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("Problem with config file" + e.getMessage());
                System.exit(1);
            }
        } else {
            properties = new ProfilerProperties();
        }
        System.out.println("Profiler properties : " + properties.toString());
        instrumentation.addTransformer(new Transformer(properties));
        Profile.init(properties);
        try {
            statisticsService = new Thread(new StatisticsService(properties));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.err.println("Problem starting statistics service:" + e.getMessage());
            System.exit(1);
        }
        statisticsService.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                // Add message to shutdown
                if (null != Profile.queueForStatistics) {
                    Profile.queueForStatistics.add(new Frame(null, null, -1));
                }
                // Wait 10 seconds to shutdown
                int i = 0;
                while (i++ < 100 && Main.statisticsService != null && Main.statisticsService.isAlive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (Main.statisticsService != null && Main.statisticsService.isAlive()) {
                    Main.statisticsService.interrupt();
                }
            }
        })
        );
    }
    

}


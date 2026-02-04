package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WireMockMappingsLoader {

    private static final Logger logger = LogManager.getLogger(WireMockMappingsLoader.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WireMockClasspathCustomizer customizer;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        logger.info("Context refreshed, looking for WireMock servers...");

        String[] beanNames = context.getBeanNamesForType(WireMockServer.class);
        logger.info("Found WireMockServer beans: {}", (Object) beanNames);

        for (String beanName : beanNames) {
            WireMockServer server = context.getBean(beanName, WireMockServer.class);
            if (server.isRunning()) {
                logger.info("Loading mappings onto '{}' WireMock server (port: {})", beanName, server.port());
                customizer.loadMappings(server);
            }
        }
    }
}

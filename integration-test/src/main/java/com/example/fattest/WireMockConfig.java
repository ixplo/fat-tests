package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class WireMockConfig {

    private static final Logger logger = LogManager.getLogger(WireMockConfig.class);
    private static final String MAPPINGS_PATH = "wiremock/mappings";

    public static WireMockServer createServer() {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.wireMockConfig().dynamicPort()
        );
        return server;
    }

    public static WireMockServer createServer(int port) {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(port)
        );
        return server;
    }

    public static WireMockServer createServerWithMappings() {
        WireMockServer server = createServer();
        server.start();
        loadMappingsFromClasspath(server);
        return server;
    }

    public static WireMockServer createServerWithMappings(int port) {
        WireMockServer server = createServer(port);
        server.start();
        loadMappingsFromClasspath(server);
        return server;
    }

    public static void loadMappingsFromClasspath(WireMockServer server) {
        logger.info("Loading WireMock mappings from classpath: {}", MAPPINGS_PATH);

        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths(MAPPINGS_PATH)
                .scan()) {

            List<Resource> resources = scanResult.getResourcesWithExtension("json").stream().toList();
            logger.info("Found {} mapping files", resources.size());

            for (Resource resource : resources) {
                try {
                    String json = new String(resource.load(), StandardCharsets.UTF_8);
                    StubMapping stubMapping = StubMapping.buildFrom(json);
                    server.addStubMapping(stubMapping);
                    logger.info("Loaded mapping: {}", resource.getPath());
                } catch (Exception e) {
                    logger.error("Failed to load mapping {}: {}", resource.getPath(), e.getMessage());
                }
            }
        }
    }
}

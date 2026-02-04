package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class WireMockClasspathCustomizer {

    private static final Logger logger = LogManager.getLogger(WireMockClasspathCustomizer.class);
    private static final String MAPPINGS_PATH = "wiremock/mappings";

    private List<StubMapping> cachedMappings;

    public void loadMappings(WireMockServer server) {
        if (cachedMappings == null) {
            cachedMappings = loadMappingsFromClasspath();
        }

        logger.info("Applying {} mappings to WireMock server", cachedMappings.size());
        cachedMappings.forEach(server::addStubMapping);
    }

    private List<StubMapping> loadMappingsFromClasspath() {
        List<StubMapping> mappings = new ArrayList<>();

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
                    mappings.add(stubMapping);
                    logger.info("Loaded mapping: {}", resource.getPath());
                } catch (Exception e) {
                    logger.error("Failed to load mapping {}: {}", resource.getPath(), e.getMessage());
                }
            }
        }

        return mappings;
    }
}

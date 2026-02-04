package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(name = "exceptions")
})
public abstract class BaseTest {

    private static final String MAPPINGS_PATH = "wiremock/mappings";

    @InjectWireMock("exceptions")
    protected WireMockServer wireMockServer;

    protected final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setupWireMock() {
        wireMockServer.resetAll();
        loadMappingsFromClasspath();
    }

    private void loadMappingsFromClasspath() {
        System.out.println("Loading WireMock mappings from: " + MAPPINGS_PATH);

        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths(MAPPINGS_PATH)
                .scan()) {

            List<Resource> resources = scanResult.getResourcesWithExtension("json").stream().toList();
            System.out.println("Found " + resources.size() + " mapping files");

            for (Resource resource : resources) {
                try {
                    String json = new String(resource.load(), StandardCharsets.UTF_8);
                    StubMapping stubMapping = StubMapping.buildFrom(json);
                    wireMockServer.addStubMapping(stubMapping);
                    System.out.println("Loaded mapping: " + resource.getPath());
                } catch (Exception e) {
                    System.err.println("Failed to load mapping " + resource.getPath() + ": " + e.getMessage());
                }
            }
        }
    }

    protected String getWireMockUrl() {
        return "http://localhost:" + wireMockServer.port();
    }
}

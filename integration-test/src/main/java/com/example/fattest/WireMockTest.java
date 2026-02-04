package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(
        name = "api-service",
        filesUnderClasspath = "wiremock"
    )
})
class WireMockTest {

    @InjectWireMock("api-service")
    private WireMockServer wireMockServer;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + wireMockServer.port();
    }

    @Test
    @DisplayName("Should have mappings loaded from classpath")
    void shouldHaveMappingsLoaded() {
        System.out.println("=== WireMock Diagnostic ===");
        System.out.println("WireMock running on port: " + wireMockServer.port());
        System.out.println("Total stub mappings loaded: " + wireMockServer.getStubMappings().size());
        System.out.println("Loaded mappings:");

        wireMockServer.getStubMappings().forEach(stub -> {
            String url = stub.getRequest().getUrlPattern() != null
                ? stub.getRequest().getUrlPattern()
                : stub.getRequest().getUrl();
            System.out.println("  - " + stub.getRequest().getMethod() + " " + url + " -> " + stub.getResponse().getStatus());
        });

        System.out.println("===========================");

        // We expect at least 7 mappings from wiremock/mappings/exceptions
        assertTrue(wireMockServer.getStubMappings().size() >= 7,
            "Expected at least 7 mappings to be loaded from wiremock/mappings/exceptions");
    }

    @Test
    @DisplayName("Should mock GET request and return JSON response")
    void shouldMockGetRequest() {
        // Arrange
        wireMockServer.stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"John Doe\", \"email\": \"john@example.com\"}")));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/api/users/1", String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("John Doe"));

        // Verify the request was made
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/users/1")));
    }

    @Test
    @DisplayName("Should mock POST request")
    void shouldMockPostRequest() {
        // Arrange
        wireMockServer.stubFor(post(urlEqualTo("/api/users"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(containing("\"name\""))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 2, \"name\": \"Jane Doe\"}")));

        // Act
        String requestBody = "{\"name\": \"Jane Doe\", \"email\": \"jane@example.com\"}";

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/api/users", entity, String.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("Jane Doe"));

        // Verify
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/users"))
                .withRequestBody(containing("Jane Doe")));
    }

    @Test
    @DisplayName("Should mock error response")
    void shouldMockErrorResponse() {
        // Arrange
        wireMockServer.stubFor(get(urlEqualTo("/api/users/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"User not found\"}")));

        // Act & Assert
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/users/999", String.class);
            fail("Expected exception to be thrown");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("User not found"));
        }
    }

    @Test
    @DisplayName("Should mock delayed response")
    void shouldMockDelayedResponse() {
        // Arrange
        wireMockServer.stubFor(get(urlEqualTo("/api/slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(500)
                        .withBody("Delayed response")));

        // Act
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/api/slow", String.class);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration >= 500, "Response should be delayed by at least 500ms");
    }

    // Tests using pre-configured exception mappings from wiremock/mappings/exceptions

    @Test
    @DisplayName("Should return 404 from pre-configured mapping")
    void shouldReturnNotFoundFromMapping() {
        // This uses the pre-configured mapping from not-found.json
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/users/0", String.class);
            fail("Expected 404 exception");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("RESOURCE_NOT_FOUND"));
        }
    }

    @Test
    @DisplayName("Should return 500 from pre-configured mapping")
    void shouldReturnInternalErrorFromMapping() {
        // This uses the pre-configured mapping from internal-server-error.json
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/error", String.class);
            fail("Expected 500 exception");
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("INTERNAL_ERROR"));
        }
    }

    @Test
    @DisplayName("Should return 429 rate limited from pre-configured mapping")
    void shouldReturnRateLimitedFromMapping() {
        // This uses the pre-configured mapping from rate-limited.json
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/rate-limited", String.class);
            fail("Expected 429 exception");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("RATE_LIMITED"));
        }
    }

    @Test
    @DisplayName("Should return 503 service unavailable from pre-configured mapping")
    void shouldReturnServiceUnavailableFromMapping() {
        // This uses the pre-configured mapping from service-unavailable.json
        try {
            restTemplate.getForEntity(getBaseUrl() + "/api/maintenance", String.class);
            fail("Expected 503 exception");
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("SERVICE_UNAVAILABLE"));
        }
    }
}

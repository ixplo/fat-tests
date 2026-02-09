package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class WireMockIT extends BaseTest {

    @Test
    @DisplayName("Should have mappings loaded from classpath")
    void shouldHaveMappingsLoaded() {
        System.out.println("=== WireMock Diagnostic ===");
        System.out.println("WireMock 'exceptions' running on port: " + wireMockServer.port());
        System.out.println("Total stub mappings loaded: " + wireMockServer.getStubMappings().size());
        System.out.println("Loaded mappings:");

        wireMockServer.getStubMappings().forEach(stub -> {
            String url = stub.getRequest().getUrlPattern() != null
                ? stub.getRequest().getUrlPattern()
                : stub.getRequest().getUrl();
            System.out.println("  - " + stub.getRequest().getMethod() + " " + url + " -> " + stub.getResponse().getStatus());
        });

        System.out.println("===========================");

        assertTrue(wireMockServer.getStubMappings().size() >= 7,
            "Expected at least 7 mappings to be loaded from wiremock/mappings/exceptions");
    }

    @Test
    @DisplayName("Should mock GET request and return JSON response")
    void shouldMockGetRequest() {
        wireMockServer.stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"John Doe\", \"email\": \"john@example.com\"}")));

        ResponseEntity<String> response = restTemplate.getForEntity(getWireMockUrl() + "/api/users/1", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("John Doe"));
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/api/users/1")));
    }

    @Test
    @DisplayName("Should mock POST request")
    void shouldMockPostRequest() {
        wireMockServer.stubFor(post(urlEqualTo("/api/users"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(containing("\"name\""))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 2, \"name\": \"Jane Doe\"}")));

        String requestBody = "{\"name\": \"Jane Doe\", \"email\": \"jane@example.com\"}";
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(getWireMockUrl() + "/api/users", entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("Jane Doe"));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/users"))
                .withRequestBody(containing("Jane Doe")));
    }

    @Test
    @DisplayName("Should mock error response")
    void shouldMockErrorResponse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/users/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"User not found\"}")));

        try {
            restTemplate.getForEntity(getWireMockUrl() + "/api/users/999", String.class);
            fail("Expected exception to be thrown");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("User not found"));
        }
    }

    @Test
    @DisplayName("Should mock delayed response")
    void shouldMockDelayedResponse() {
        wireMockServer.stubFor(get(urlEqualTo("/api/slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(500)
                        .withBody("Delayed response")));

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity(getWireMockUrl() + "/api/slow", String.class);
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(duration >= 500, "Response should be delayed by at least 500ms");
    }

    // Tests using pre-configured exception mappings

    @Test
    @DisplayName("Should return 404 from pre-configured mapping")
    void shouldReturnNotFoundFromMapping() {
        try {
            restTemplate.getForEntity(getWireMockUrl() + "/api/users/0", String.class);
            fail("Expected 404 exception");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("RESOURCE_NOT_FOUND"));
        }
    }

    @Test
    @DisplayName("Should return 500 from pre-configured mapping")
    void shouldReturnInternalErrorFromMapping() {
        try {
            restTemplate.getForEntity(getWireMockUrl() + "/api/error", String.class);
            fail("Expected 500 exception");
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("INTERNAL_ERROR"));
        }
    }

    @Test
    @DisplayName("Should return 429 rate limited from pre-configured mapping")
    void shouldReturnRateLimitedFromMapping() {
        try {
            restTemplate.getForEntity(getWireMockUrl() + "/api/rate-limited", String.class);
            fail("Expected 429 exception");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("RATE_LIMITED"));
        }
    }

    @Test
    @DisplayName("Should return 503 service unavailable from pre-configured mapping")
    void shouldReturnServiceUnavailableFromMapping() {
        try {
            restTemplate.getForEntity(getWireMockUrl() + "/api/maintenance", String.class);
            fail("Expected 503 exception");
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
            assertTrue(e.getResponseBodyAsString().contains("SERVICE_UNAVAILABLE"));
        }
    }
}

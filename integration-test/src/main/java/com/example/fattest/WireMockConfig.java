package com.example.fattest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * WireMock configuration helper for non-Spring test scenarios.
 * For Spring Boot tests, prefer using @EnableWireMock annotation instead.
 */
public class WireMockConfig {

    public static WireMockServer createServer() {
        return new WireMockServer(
                WireMockConfiguration.wireMockConfig().dynamicPort()
        );
    }

    public static WireMockServer createServer(int port) {
        return new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(port)
        );
    }
}

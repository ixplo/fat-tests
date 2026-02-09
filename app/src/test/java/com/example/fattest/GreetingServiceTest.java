package com.example.fattest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreetingServiceTest {

    private AppConfig appConfig;
    private GreetingService greetingService;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
        appConfig.getGreeting().setPrefix("Hello");
        appConfig.getGreeting().setSuffix("!");
        appConfig.setEnvironment("test");
        appConfig.getFeature().setEnabled(true);
        greetingService = new GreetingService(appConfig);
    }

    @Test
    void greet_withName_returnsPersonalizedGreeting() {
        String result = greetingService.greet("John");
        assertEquals("Hello, John!", result);
    }

    @Test
    void greet_withNullName_returnsDefaultGreeting() {
        String result = greetingService.greet(null);
        assertEquals("Hello, World!", result);
    }

    @Test
    void greet_withBlankName_returnsDefaultGreeting() {
        String result = greetingService.greet("   ");
        assertEquals("Hello, World!", result);
    }

    @Test
    void greet_withEmptyName_returnsDefaultGreeting() {
        String result = greetingService.greet("");
        assertEquals("Hello, World!", result);
    }

    @Test
    void greet_withCustomPrefixAndSuffix_returnsCustomGreeting() {
        appConfig.getGreeting().setPrefix("Hi");
        appConfig.getGreeting().setSuffix("!!!");

        String result = greetingService.greet("Alice");
        assertEquals("Hi, Alice!!!", result);
    }

    @Test
    void getEnvironment_returnsConfiguredEnvironment() {
        assertEquals("test", greetingService.getEnvironment());
    }

    @Test
    void isFeatureEnabled_whenEnabled_returnsTrue() {
        assertTrue(greetingService.isFeatureEnabled());
    }

    @Test
    void isFeatureEnabled_whenDisabled_returnsFalse() {
        appConfig.getFeature().setEnabled(false);
        assertFalse(greetingService.isFeatureEnabled());
    }
}

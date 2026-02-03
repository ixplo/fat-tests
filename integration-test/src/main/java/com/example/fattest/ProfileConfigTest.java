package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfileConfigTest {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Should load correct configuration for active profile")
    void shouldLoadCorrectConfiguration() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        System.out.println("==========================================");
        System.out.println("Profile Configuration Test");
        System.out.println("==========================================");
        System.out.println("Active profile: " + profile);
        System.out.println("Environment: " + appConfig.getEnvironment());
        System.out.println("Greeting prefix: " + appConfig.getGreeting().getPrefix());
        System.out.println("Greeting suffix: " + appConfig.getGreeting().getSuffix());
        System.out.println("Feature enabled: " + appConfig.getFeature().isEnabled());
        System.out.println("==========================================");

        switch (profile) {
            case "dev3" -> {
                assertEquals("dev3", appConfig.getEnvironment());
                assertEquals("Dev3", appConfig.getGreeting().getPrefix());
                assertEquals("!", appConfig.getGreeting().getSuffix());
                assertTrue(appConfig.getFeature().isEnabled());
            }
            case "dev4" -> {
                assertEquals("dev4", appConfig.getEnvironment());
                assertEquals("Dev4", appConfig.getGreeting().getPrefix());
                assertEquals("!!!", appConfig.getGreeting().getSuffix());
                assertFalse(appConfig.getFeature().isEnabled());
            }
            default -> {
                assertEquals("default", appConfig.getEnvironment());
                assertEquals("Hello", appConfig.getGreeting().getPrefix());
                assertEquals("!", appConfig.getGreeting().getSuffix());
                assertTrue(appConfig.getFeature().isEnabled());
            }
        }
    }

    @Test
    @DisplayName("Should have non-null configuration values")
    void shouldHaveNonNullConfigValues() {
        assertNotNull(appConfig.getEnvironment(), "Environment should not be null");
        assertNotNull(appConfig.getGreeting(), "Greeting config should not be null");
        assertNotNull(appConfig.getGreeting().getPrefix(), "Greeting prefix should not be null");
        assertNotNull(appConfig.getGreeting().getSuffix(), "Greeting suffix should not be null");
        assertNotNull(appConfig.getFeature(), "Feature config should not be null");
    }
}

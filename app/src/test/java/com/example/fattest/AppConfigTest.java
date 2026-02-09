package com.example.fattest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void defaultValues_areCorrect() {
        AppConfig config = new AppConfig();

        assertEquals("default", config.getEnvironment());
        assertEquals("Hello", config.getGreeting().getPrefix());
        assertEquals("!", config.getGreeting().getSuffix());
        assertTrue(config.getFeature().isEnabled());
    }

    @Test
    void setEnvironment_updatesValue() {
        AppConfig config = new AppConfig();
        config.setEnvironment("production");

        assertEquals("production", config.getEnvironment());
    }

    @Test
    void setGreeting_updatesNestedValues() {
        AppConfig config = new AppConfig();
        AppConfig.Greeting greeting = new AppConfig.Greeting();
        greeting.setPrefix("Hi");
        greeting.setSuffix("?");
        config.setGreeting(greeting);

        assertEquals("Hi", config.getGreeting().getPrefix());
        assertEquals("?", config.getGreeting().getSuffix());
    }

    @Test
    void setFeature_updatesNestedValues() {
        AppConfig config = new AppConfig();
        AppConfig.Feature feature = new AppConfig.Feature();
        feature.setEnabled(false);
        config.setFeature(feature);

        assertFalse(config.getFeature().isEnabled());
    }
}

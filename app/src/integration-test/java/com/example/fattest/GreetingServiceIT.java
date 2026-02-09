package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GreetingServiceIT {

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Should greet with configured prefix when name is provided")
    void shouldGreetWithName() {
        String result = greetingService.greet("Alice");
        String[] activeProfiles = environment.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        System.out.println("Running with profile: " + profile);
        System.out.println("Greeting result: " + result);

        assertTrue(result.contains("Alice"), "Greeting should contain the name");
        assertTrue(result.contains(","), "Greeting should have proper format");
    }

    @Test
    @DisplayName("Should greet with World when name is null")
    void shouldGreetWithWorldWhenNameIsNull() {
        String result = greetingService.greet(null);
        assertTrue(result.contains("World"), "Should greet World when name is null");
    }

    @Test
    @DisplayName("Should greet with World when name is blank")
    void shouldGreetWithWorldWhenNameIsBlank() {
        String result = greetingService.greet("   ");
        assertTrue(result.contains("World"), "Should greet World when name is blank");
    }

    @Test
    @DisplayName("Should report correct environment from config")
    void shouldReportCorrectEnvironment() {
        String env = greetingService.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();

        System.out.println("Environment from config: " + env);
        System.out.println("Active profiles: " + String.join(", ", activeProfiles));

        assertNotNull(env, "Environment should not be null");

        if (activeProfiles.length > 0) {
            assertEquals(activeProfiles[0], env, "Environment should match active profile");
        } else {
            assertEquals("default", env, "Environment should be 'default' when no profile is active");
        }
    }
}

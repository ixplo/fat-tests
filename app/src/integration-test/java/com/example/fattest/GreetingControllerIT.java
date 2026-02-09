package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Should return greeting with name parameter using profile config")
    void shouldReturnGreetingWithName() throws Exception {
        String prefix = appConfig.getGreeting().getPrefix();
        String suffix = appConfig.getGreeting().getSuffix();
        String expected = prefix + ", Bob" + suffix;

        System.out.println("Testing with profile: " + String.join(", ", environment.getActiveProfiles()));
        System.out.println("Expected greeting: " + expected);

        mockMvc.perform(get("/greet").param("name", "Bob"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @DisplayName("Should return default greeting without name parameter using profile config")
    void shouldReturnDefaultGreeting() throws Exception {
        String prefix = appConfig.getGreeting().getPrefix();
        String suffix = appConfig.getGreeting().getSuffix();
        String expected = prefix + ", World" + suffix;

        System.out.println("Expected default greeting: " + expected);

        mockMvc.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }
}

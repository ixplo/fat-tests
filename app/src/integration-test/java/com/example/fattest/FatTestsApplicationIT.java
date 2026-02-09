package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FatTestsApplicationIT {

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        // Verifies that the Spring application context loads without errors
    }
}

package com.example.fattest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CsvDataTest {

    @Test
    @DisplayName("Should load users.csv and verify data")
    void shouldLoadUsersData() throws Exception {
        List<String[]> users = loadCsv("data/users.csv");

        assertFalse(users.isEmpty(), "Users list should not be empty");
        assertEquals(6, users.size(), "Should have header + 5 users");

        // Verify header
        String[] header = users.get(0);
        assertArrayEquals(new String[]{"id", "name", "email", "active"}, header);

        // Verify first user
        String[] alice = users.get(1);
        assertEquals("1", alice[0]);
        assertEquals("Alice", alice[1]);
        assertEquals("alice@example.com", alice[2]);
        assertEquals("true", alice[3]);

        System.out.println("Loaded " + (users.size() - 1) + " users from CSV");
    }

    @Test
    @DisplayName("Should load products.csv and verify data")
    void shouldLoadProductsData() throws Exception {
        List<String[]> products = loadCsv("data/products.csv");

        assertFalse(products.isEmpty(), "Products list should not be empty");
        assertEquals(6, products.size(), "Should have header + 5 products");

        // Verify header
        String[] header = products.get(0);
        assertArrayEquals(new String[]{"id", "name", "price", "category"}, header);

        // Verify laptop
        String[] laptop = products.get(1);
        assertEquals("Laptop", laptop[1]);
        assertEquals("999.99", laptop[2]);
        assertEquals("Electronics", laptop[3]);

        System.out.println("Loaded " + (products.size() - 1) + " products from CSV");
    }

    @Test
    @DisplayName("Should filter active users from CSV")
    void shouldFilterActiveUsers() throws Exception {
        List<String[]> users = loadCsv("data/users.csv");

        long activeCount = users.stream()
                .skip(1) // skip header
                .filter(row -> "true".equals(row[3]))
                .count();

        assertEquals(3, activeCount, "Should have 3 active users");
        System.out.println("Found " + activeCount + " active users");
    }

    @Test
    @DisplayName("Should calculate total price of electronics")
    void shouldCalculateElectronicsTotal() throws Exception {
        List<String[]> products = loadCsv("data/products.csv");

        double total = products.stream()
                .skip(1) // skip header
                .filter(row -> "Electronics".equals(row[3]))
                .mapToDouble(row -> Double.parseDouble(row[2]))
                .sum();

        assertEquals(1509.96, total, 0.01, "Electronics total should be 1509.96");
        System.out.println("Electronics total: $" + total);
    }

    private List<String[]> loadCsv(String path) throws Exception {
        List<String[]> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(path);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(","));
            }
        }
        return rows;
    }
}

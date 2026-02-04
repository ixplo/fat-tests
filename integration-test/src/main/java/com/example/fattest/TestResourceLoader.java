package com.example.fattest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class TestResourceLoader {

    private static final Logger logger = LogManager.getLogger(TestResourceLoader.class);

    private static Path tempDir;

    /**
     * Load a classpath resource as a File.
     * Works both in IDE and fat jar environments.
     *
     * @param path classpath location e.g. "data/users.csv"
     * @return File object that can be used with APIs requiring File
     */
    public File getFile(String path) throws IOException {
        return loadFile(path);
    }

    /**
     * Static method for use without Spring context.
     */
    public static File loadFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);

        try {
            // Try direct file access first (works in IDE)
            File file = resource.getFile();
            logger.debug("Loaded resource as file: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            // Fall back to extracting to temp file (works in fat jar)
            logger.debug("Resource not accessible as file, extracting to temp: {}", path);
            return extractToTempFile(resource, path);
        }
    }

    /**
     * Load a classpath resource as a Path.
     */
    public Path getPath(String path) throws IOException {
        return getFile(path).toPath();
    }

    /**
     * Static method for use without Spring context.
     */
    public static Path loadPath(String path) throws IOException {
        return loadFile(path).toPath();
    }

    /**
     * Get InputStream for a classpath resource.
     * Prefer this when you don't need a File.
     */
    public InputStream getInputStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

    /**
     * Static method for use without Spring context.
     */
    public static InputStream loadInputStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

    private static File extractToTempFile(ClassPathResource resource, String originalPath) throws IOException {
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("test-resources-");
            tempDir.toFile().deleteOnExit();
            logger.info("Created temp directory for resources: {}", tempDir);
        }

        Path targetPath = tempDir.resolve(originalPath);

        // Create parent directories if needed
        Files.createDirectories(targetPath.getParent());

        // Copy resource to temp file
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        File file = targetPath.toFile();
        file.deleteOnExit();
        logger.debug("Extracted resource to temp file: {}", file.getAbsolutePath());

        return file;
    }
}

package com.example.fattest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestResourceLoader {

    private static final Logger logger = LogManager.getLogger(TestResourceLoader.class);

    private static Path tempDir;

    // ==================== File-based methods (requires extraction in fat jar) ====================

    /**
     * Load a classpath resource as a File.
     * Works both in IDE and fat jar environments.
     * Note: In fat jar, extracts to temp file.
     */
    public File getFile(String path) throws IOException {
        return loadFile(path);
    }

    public static File loadFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);

        try {
            File file = resource.getFile();
            logger.debug("Loaded resource as file: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            logger.debug("Resource not accessible as file, extracting to temp: {}", path);
            return extractToTempFile(resource, path);
        }
    }

    public Path getPath(String path) throws IOException {
        return getFile(path).toPath();
    }

    public static Path loadPath(String path) throws IOException {
        return loadFile(path).toPath();
    }

    // ==================== Stream-based methods (no extraction needed) ====================

    /**
     * Get InputStream for a classpath resource.
     * Works directly in fat jar without extraction.
     */
    public InputStream getInputStream(String path) throws IOException {
        return loadInputStream(path);
    }

    public static InputStream loadInputStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

    /**
     * Get Spring Resource for a classpath resource.
     * Works directly in fat jar without extraction.
     */
    public Resource getResource(String path) {
        return loadResource(path);
    }

    public static Resource loadResource(String path) {
        return new ClassPathResource(path);
    }

    // ==================== Content-based methods (no extraction needed) ====================

    /**
     * Load resource content as byte array.
     * Works directly in fat jar without extraction.
     */
    public byte[] getBytes(String path) throws IOException {
        return loadBytes(path);
    }

    public static byte[] loadBytes(String path) throws IOException {
        try (InputStream is = loadInputStream(path)) {
            return is.readAllBytes();
        }
    }

    /**
     * Load resource content as String (UTF-8).
     * Works directly in fat jar without extraction.
     */
    public String getString(String path) throws IOException {
        return loadString(path);
    }

    public static String loadString(String path) throws IOException {
        return loadString(path, StandardCharsets.UTF_8);
    }

    public String getString(String path, Charset charset) throws IOException {
        return loadString(path, charset);
    }

    public static String loadString(String path, Charset charset) throws IOException {
        return new String(loadBytes(path), charset);
    }

    /**
     * Load resource content as list of lines (UTF-8).
     * Works directly in fat jar without extraction.
     */
    public List<String> getLines(String path) throws IOException {
        return loadLines(path);
    }

    public static List<String> loadLines(String path) throws IOException {
        return loadLines(path, StandardCharsets.UTF_8);
    }

    public List<String> getLines(String path, Charset charset) throws IOException {
        return loadLines(path, charset);
    }

    public static List<String> loadLines(String path, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(loadInputStream(path), charset))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    // ==================== Helper methods ====================

    private static File extractToTempFile(ClassPathResource resource, String originalPath) throws IOException {
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("test-resources-");
            tempDir.toFile().deleteOnExit();
            logger.info("Created temp directory for resources: {}", tempDir);
        }

        Path targetPath = tempDir.resolve(originalPath);
        Files.createDirectories(targetPath.getParent());

        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        File file = targetPath.toFile();
        file.deleteOnExit();
        logger.debug("Extracted resource to temp file: {}", file.getAbsolutePath());

        return file;
    }
}

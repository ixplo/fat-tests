package com.example.fattest;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    private static final String TEST_PACKAGE = "com.example.fattest";

    public static void main(String[] args) {
        String profile = parseProfile(args);

        System.out.println("==========================================");
        System.out.println("       Fat Tests - Test Runner");
        System.out.println("==========================================");
        System.out.println();

        if (profile != null) {
            System.out.println("Active profile: " + profile);
            System.setProperty("spring.profiles.active", profile);
        } else {
            System.out.println("Active profile: default");
        }

        System.out.println("Test package: " + TEST_PACKAGE);
        System.out.println();

        // Scan for test classes using ClassGraph (works with Spring Boot fat jars)
        List<Class<?>> testClasses = scanTestClassesInPackage(TEST_PACKAGE);
        System.out.println("Found " + testClasses.size() + " test classes in package:");
        testClasses.forEach(c -> System.out.println("  - " + c.getSimpleName()));
        System.out.println();

        if (testClasses.isEmpty()) {
            System.err.println("No test classes found!");
            System.exit(1);
        }

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        // Use selectPackage with the discovered classes
        LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(TEST_PACKAGE))
                .filters(ClassNameFilter.includeClassNamePatterns(".*Test$"));

        // Also add individual class selectors to ensure discovery works in fat jar
        for (Class<?> testClass : testClasses) {
            requestBuilder.selectors(DiscoverySelectors.selectClass(testClass));
        }

        LauncherDiscoveryRequest request = requestBuilder.build();

        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();

        System.out.println();
        System.out.println("==========================================");
        System.out.println("              TEST RESULTS");
        System.out.println("==========================================");

        PrintWriter writer = new PrintWriter(System.out);
        summary.printTo(writer);
        summary.printFailuresTo(writer);
        writer.flush();

        System.out.println();
        System.out.println("Tests found:     " + summary.getTestsFoundCount());
        System.out.println("Tests started:   " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed:    " + summary.getTestsFailedCount());
        System.out.println("Tests skipped:   " + summary.getTestsSkippedCount());
        System.out.println("==========================================");

        if (summary.getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }

    private static List<Class<?>> scanTestClassesInPackage(String packageName) {
        List<Class<?>> testClasses = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageName)
                .addClassLoader(Thread.currentThread().getContextClassLoader())
                .enableAllInfo()
                .ignoreClassVisibility()
                .scan()) {

            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                String className = classInfo.getName();
                if (className.endsWith("Test")) {
                    try {
                        testClasses.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        System.err.println("Could not load: " + className);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ClassGraph scan error: " + e.getMessage());
        }

        return testClasses;
    }

    private static String parseProfile(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--profile".equals(args[i]) && i + 1 < args.length) {
                return args[i + 1];
            }
            if (args[i].startsWith("--profile=")) {
                return args[i].substring("--profile=".length());
            }
            if (args[i].startsWith("--spring.profiles.active=")) {
                return args[i].substring("--spring.profiles.active=".length());
            }
        }
        return null;
    }
}

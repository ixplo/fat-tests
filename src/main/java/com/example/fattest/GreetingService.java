package com.example.fattest;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private final AppConfig appConfig;

    public GreetingService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String greet(String name) {
        String prefix = appConfig.getGreeting().getPrefix();
        String suffix = appConfig.getGreeting().getSuffix();

        if (name == null || name.isBlank()) {
            return prefix + ", World" + suffix;
        }
        return prefix + ", " + name + suffix;
    }

    public String getEnvironment() {
        return appConfig.getEnvironment();
    }

    public boolean isFeatureEnabled() {
        return appConfig.getFeature().isEnabled();
    }
}

package com.example.fattest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private String environment = "default";
    private Greeting greeting = new Greeting();
    private Feature feature = new Feature();

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Greeting getGreeting() {
        return greeting;
    }

    public void setGreeting(Greeting greeting) {
        this.greeting = greeting;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public static class Greeting {
        private String prefix = "Hello";
        private String suffix = "!";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }

    public static class Feature {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

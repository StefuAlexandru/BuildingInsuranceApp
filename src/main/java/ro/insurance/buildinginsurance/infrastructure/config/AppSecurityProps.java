package ro.insurance.buildinginsurance.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProps(User admin, User broker) {
    public record User(String username, String password) {}
}


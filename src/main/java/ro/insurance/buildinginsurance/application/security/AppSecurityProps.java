package ro.insurance.buildinginsurance.application.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProps(Admin admin, Broker broker) {
    public record Admin(String username, String password) {}
    public record Broker(String password) {}
}

package ro.insurance.buildinginsurance.application.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        long accessMinutes,
        long refreshDays
) {}

package ro.insurance.buildinginsurance.util.data_factory;

import ro.insurance.buildinginsurance.domain.entity.RefreshToken;

import java.time.OffsetDateTime;

public final class RefreshTokenTestDataFactory {

    private RefreshTokenTestDataFactory() {}

    public static RefreshToken aRefreshToken() {
        return RefreshToken.builder()
                .token("rftoken1234567890rftoken1234567890rftoken1234567890abcd")
                .email("broker@email.com")
                .expiresAt(OffsetDateTime.now().plusDays(14))
                .revoked(false)
                .build();
    }
}

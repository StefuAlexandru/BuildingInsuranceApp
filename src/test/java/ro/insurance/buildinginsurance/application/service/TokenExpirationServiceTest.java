package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.TokenExpirationService;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RefreshTokenRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenExpirationServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @Test
    void expireTokens_shouldUseClockToday_andReturnDeletedCount() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-04T10:00:00Z"), ZoneOffset.UTC);
        TokenExpirationService tokenExpirationService = new TokenExpirationService(refreshTokenRepository, clock);

        when(refreshTokenRepository.deleteExpiredTokens(any())).thenReturn(7);

        int deletedCount = tokenExpirationService.deleteExpiredTokens();

        assertEquals(7, deletedCount);

        ArgumentCaptor<OffsetDateTime> dateCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(refreshTokenRepository).deleteExpiredTokens(dateCaptor.capture());
        assertEquals(clock.instant(), dateCaptor.getValue().toInstant());

        verifyNoMoreInteractions(refreshTokenRepository);
    }
}

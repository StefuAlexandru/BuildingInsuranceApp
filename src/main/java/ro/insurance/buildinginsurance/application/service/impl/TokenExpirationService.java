package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.ITokenExpirationService;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RefreshTokenRepository;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TokenExpirationService implements ITokenExpirationService {

    private final RefreshTokenRepository tokenRepository;
    private final Clock clock;

    @Override
    public int deleteExpiredTokens() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        int expiredCount = tokenRepository.deleteExpiredTokens(now);

        if(expiredCount > 0) {
            log.info("Deleted {} refresh tokens at {}", expiredCount, now);
        }
        return expiredCount;
    }
}

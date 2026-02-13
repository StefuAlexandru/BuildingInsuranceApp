package ro.insurance.buildinginsurance.application.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.insurance.buildinginsurance.application.service.ITokenExpirationService;

@Component
@RequiredArgsConstructor
public class TokenExpirationScheduler {

    private final ITokenExpirationService tokenExpirationService;

    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC")
    public void deleteExpiredTokens() {tokenExpirationService.deleteExpiredTokens();}

}

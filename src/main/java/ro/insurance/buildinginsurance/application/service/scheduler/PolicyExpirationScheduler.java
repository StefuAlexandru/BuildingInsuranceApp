package ro.insurance.buildinginsurance.application.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.insurance.buildinginsurance.application.service.IPolicyExpirationService;

@Component
@RequiredArgsConstructor
public class PolicyExpirationScheduler {

    private final IPolicyExpirationService policyExpirationService;

    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    public void expirePoliciesDaily(){policyExpirationService.expirePolicies();}
}

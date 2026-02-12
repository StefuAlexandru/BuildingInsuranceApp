package ro.insurance.buildinginsurance.application.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.insurance.buildinginsurance.application.service.impl.PolicyExpirationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyExpirationScheduler {
    private final PolicyExpirationService policyExpirationService;

    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    public void expirePoliciesDaily(){
        policyExpirationService.expirePolicies();
    }
}

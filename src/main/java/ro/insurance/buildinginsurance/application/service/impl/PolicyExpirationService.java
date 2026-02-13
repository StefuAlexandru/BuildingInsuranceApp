package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.IPolicyExpirationService;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PolicyExpirationService implements IPolicyExpirationService {

    private final PolicyRepository policyRepository;
    private final Clock clock;

    @Override
    public int expirePolicies() {
        LocalDate today = LocalDate.now(clock);
        int updated = policyRepository.expireActivePolicies(today, PolicyStatus.ACTIVE,PolicyStatus.EXPIRED);

        if(updated > 0) {
            log.info("Expired {} policies (endDate < {})", updated, today);
        }
        return updated;
    }
}

package ro.insurance.buildinginsurance.application.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ro.insurance.buildinginsurance.domain.entity.Broker;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BrokerRepository;
import ro.insurance.buildinginsurance.web.error.BusinessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentBrokerProvider {

    private final BrokerRepository brokerRepository;

    public Broker getOrThrow(String email) {
        return brokerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("Broker not found for email={}", email);
                    return new BusinessException(ErrorCode.BROKER_NOT_FOUND);
                });
    }

    public Broker getActiveOrThrow(String email) {
        Broker broker = getOrThrow(email);
        if (!broker.isActive()) {
            log.warn("Broker inactive for email={}, brokerId={}", email, broker.getId());
            throw new BusinessException(ErrorCode.BROKER_IS_NOT_ACTIVE);
        }
        return broker;
    }
}

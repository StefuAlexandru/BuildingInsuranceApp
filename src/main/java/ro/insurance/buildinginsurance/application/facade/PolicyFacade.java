package ro.insurance.buildinginsurance.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCreateRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyPatchRequestDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PolicyFacade {

    private final ClientRepository clientRepository;
    private final BuildingRepository buildingRepository;
    private final BrokerRepository brokerRepository;
    private final CurrencyRepository currencyRepository;
    private final Clock clock;


    public PolicyCreateContext resolveCreateContext(PolicyCreateRequestDTO request) {
        Client client = getClientOrThrow(request.clientId());
        Building building = getBuildingOrThrow(request.buildingId());
        validateBuildingOwnership(request.buildingId(), request.clientId());
        Broker broker = getBrokerOrThrow(request.brokerId());
        validateBrokerActive(broker, request.brokerId());
        Currency currency = getCurrencyOrThrow(request.currencyId());

        validateCurrencyActive(currency, request.currencyId());
        validateDates(request.startDate(), request.endDate());

        return new PolicyCreateContext(client,broker,building,currency);
    }

    public void validateActivateOrThrow(Policy policy){
        validatePolicyStatus(policy);

        LocalDate today = LocalDate.now(clock);
        if (policy.getStartDate().isBefore(today)) {
            log.warn("Policy {} cannot be activated because start date {} is in the past", policy.getId(), policy.getStartDate());
            throw new BusinessException(ErrorCode.POLICY_START_DATE_IN_PAST);
        }

        if (policy.getClient() == null || policy.getBuilding() == null || policy.getBroker() == null || policy.getCurrency() == null
                || policy.getEndDate() == null || policy.getBasePremiumAmount() == null) {
            log.warn("Policy {} invalid state for activation (missing mandatory fields)", policy.getId());
            throw new BusinessException(ErrorCode.POLICY_INVALID_STATE);
        }

        validateBrokerActive(policy.getBroker(), policy.getBroker().getId());
        validateCurrencyActive(policy.getCurrency(), policy.getCurrency().getId());
    }

    public void validateCancelOrThrow(Policy policy) {
        if(policy.getStatus() != PolicyStatus.ACTIVE){
            log.warn("Policy with id {} cannot be cancelled because status is {}", policy.getId(), policy.getStatus());
            throw new BusinessException(ErrorCode.POLICY_NOT_CANCELLABLE);
        }
        if (policy.getCancelledAt() != null) {
            log.warn("Policy {} cannot be cancelled because cancelledAt is already set {}", policy.getId(), policy.getCancelledAt());
            throw new BusinessException(ErrorCode.POLICY_NOT_CANCELLABLE);
        }
    }

    public PolicyPatchContext validatePatchOrThrow(Policy policy, PolicyPatchRequestDTO request){
        Long buildingId = request.buildingId();
        Long currencyId = request.currencyId();
        BigDecimal basePremiumAmount = request.basePremiumAmount();

        Building building = policy.getBuilding();
        Currency currency = policy.getCurrency();

        validatePolicyStatus(policy);

        if(buildingId != null){
            building = getBuildingOrThrow(buildingId);
            validateBuildingOwnership(building.getId(),policy.getClient().getId());
        }

        if (currencyId != null) {
            if(basePremiumAmount == null){
                log.warn("Patch failed: currencyId provided but basePremiumAmount missing. policyId={}", policy.getId());
                throw new BusinessException(ErrorCode.BASE_PREMIUM_REQUIRED_WHEN_CHANGING_CURRENCY);
            }
            currency = getCurrencyOrThrow(currencyId);
            validateCurrencyActive(currency, currencyId);
        }

        LocalDate startDate = request.startDate() != null ? request.startDate() : policy.getStartDate();
        LocalDate endDate =  request.endDate() != null ? request.endDate() : policy.getEndDate();

        validateDates(startDate, endDate);

        return new PolicyPatchContext(building,currency);
    }

    // --------------------  HELPERS  --------------------

    private void validatePolicyStatus(Policy policy) {
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            log.warn("Policy {} cannot be modified because status is {}", policy.getId(), policy.getStatus());
            throw new BusinessException(ErrorCode.POLICY_NOT_DRAFT);
        }
    }

    private void validateBrokerActive(Broker broker, Long brokerId) {
        if (!broker.isActive()) {
            log.warn("Broker with id {} is not active", brokerId);
            throw new BusinessException(ErrorCode.BROKER_IS_NOT_ACTIVE);
        }
    }

    private Client getClientOrThrow(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client with id {} does not exist for creating/modifying a draft policy", clientId);
                    return new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
                });
    }

    private Building getBuildingOrThrow(Long buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> {
                    log.warn("Building with id {} does not exist for creating/modifying a draft policy", buildingId);
                    return new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
                });
    }

    private void validateBuildingOwnership(Long buildingId, Long clientId) {
        if (!buildingRepository.existsByIdAndClientId(buildingId, clientId)) {
            log.warn("Building with id {} is not owned by client with id {}", buildingId, clientId);
            throw new BusinessException(ErrorCode.BUILDING_NOT_OWNED_BY_CLIENT);
        }
    }

    private Broker getBrokerOrThrow(Long brokerId) {
        return brokerRepository.findById(brokerId)
                .orElseThrow(() -> {
                    log.warn("Broker with id {} does not exist for creating/modifying a draft policy", brokerId);
                    return new BusinessException(ErrorCode.BROKER_NOT_FOUND);
                });
    }

    private Currency getCurrencyOrThrow(Long currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> {
                    log.warn("Currency with id {} does not exist for creating/modifying a draft policy", currencyId);
                    return new BusinessException(ErrorCode.CURRENCY_NOT_FOUND);
                });
    }

    private void validateCurrencyActive(Currency currency, Long currencyId) {
        if (!currency.isActive()) {
            log.warn("Currency with id {} is not active for creating/modifying a draft policy", currencyId);
            throw new BusinessException(ErrorCode.CURRENCY_IS_NOT_ACTIVE);
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            log.warn("Invalid dates for creating/modifying draft policy. Start date {} is after end date {}", startDate, endDate);
            throw new BusinessException(ErrorCode.INVALID_DATES);
        }
    }

    public record PolicyCreateContext(Client client,Broker broker,Building building,Currency currency) {}
    public record PolicyPatchContext(Building building,Currency currency) {}
}

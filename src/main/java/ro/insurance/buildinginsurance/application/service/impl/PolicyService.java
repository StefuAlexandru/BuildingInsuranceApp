package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.facade.PolicyFacade;
import ro.insurance.buildinginsurance.application.mapper.PolicyMapper;
import ro.insurance.buildinginsurance.application.service.IPolicyCalculationService;
import ro.insurance.buildinginsurance.application.service.IPolicyService;
import ro.insurance.buildinginsurance.domain.entity.Policy;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.util.PolicyNumberGenerator;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static ro.insurance.buildinginsurance.infrastructure.persistance.spec.PolicySpecifications.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PolicyService implements IPolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final PolicyFacade policyFacade;
    private final IPolicyCalculationService policyCalculationService;
    private final PolicyNumberGenerator policyNumberGenerator;
    private final Clock clock;

    // --------------------- CREATE ---------------------

    @Override
    public PolicyResponseDTO createDraftPolicy(PolicyCreateRequestDTO policyCreateRequestDTO) {
        log.info("Create draft policy request: clientId={}, buildingId={}, brokerId={}, currencyId={}",
                policyCreateRequestDTO.clientId(),
                policyCreateRequestDTO.buildingId(),
                policyCreateRequestDTO.brokerId(),
                policyCreateRequestDTO.currencyId());

        var policyCreateContext = policyFacade.resolveCreateContext(policyCreateRequestDTO);
        Policy entity = policyMapper.toEntity(policyCreateRequestDTO,
                policyCreateContext.client(),
                policyCreateContext.building(),
                policyCreateContext.broker(),
                policyCreateContext.currency());

        PolicyCalculationRequestDTO policyCalculationRequestDTO = new PolicyCalculationRequestDTO(
                policyCreateRequestDTO.basePremiumAmount(),
                policyCreateRequestDTO.buildingId(),
                policyCreateRequestDTO.currencyId(),
                LocalDate.now(clock)
        );

        PolicyCalculationResponseDTO calculationResponse = policyCalculationService.calculatePremium(policyCalculationRequestDTO);

        entity.setFinalPremiumAmount(calculationResponse.finalPremium());
        entity.setPolicyNumber(policyNumberGenerator.generatePolicyNumber());

        Policy saved = policyRepository.save(entity);
        return policyMapper.toResponse(saved,calculationResponse);
    }

    // --------------------- PATCH ---------------------

    @Override
    public PolicyResponseDTO activatePolicy(Long id) {
        log.info("Policy activate requested. policyId={}", id);

        Policy policy = getPolicyOrThrow(id);

        policyFacade.validateActivateOrThrow(policy);

        PolicyCalculationRequestDTO calcReq = new PolicyCalculationRequestDTO(
                policy.getBasePremiumAmount(),
                policy.getBuilding().getId(),
                policy.getCurrency().getId(),
                LocalDate.now(clock)
        );

        PolicyCalculationResponseDTO calculationResponse = policyCalculationService.calculatePremium(calcReq);
        log.info("Premium calculated. policyId={} finalPremium={}", id, calculationResponse.finalPremium());

        policy.setFinalPremiumAmount(calculationResponse.finalPremium());

        BigDecimal commissionPct = policy.getBroker().getCommissionPct();
        BigDecimal commissionAmount = policy.getFinalPremiumAmount()
                .multiply(commissionPct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        policy.setBrokerCommissionPct(commissionPct);
        policy.setBrokerCommissionAmount(commissionAmount);

        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setActivatedAt(OffsetDateTime.now(clock));

        Policy saved = policyRepository.save(policy);
        log.info("Policy activated successfully. policyId={} status={} activatedAt={}",
                saved.getId(), saved.getStatus(), saved.getActivatedAt());
        return policyMapper.toResponse(saved,calculationResponse);
    }

    @Override
    public PolicyResponseDTO cancelPolicy(Long id, PolicyCancelRequestDTO request) {
        log.info("Policy cancel requested. policyId={} reason='{}'", id, request.cancellationReason());

        Policy policy = getPolicyOrThrow(id);
        policyFacade.validateCancelOrThrow(policy);

        OffsetDateTime cancelledAt = OffsetDateTime.now(clock);

        // you can cancel even before startDate, but NOT after coverage ended
        OffsetDateTime endExclusive = policy.getEndDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime();

        if (!cancelledAt.isBefore(endExclusive)) {  // cancelledAt >= endExclusive
            log.warn("Policy cancellation out of range. policyId={} cancelledAt={} endDate={}",
                    id, cancelledAt, policy.getEndDate());
            throw new BusinessException(ErrorCode.POLICY_CANCELLATION_DATE_OUT_OF_RANGE);
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setCancelledAt(cancelledAt);
        policy.setCancellationReason(request.cancellationReason());

        Policy saved = policyRepository.save(policy);

        log.info("Policy cancelled successfully. policyId={} status={} cancelledAt={}",
                saved.getId(), saved.getStatus(), saved.getCancelledAt());

        PolicyCalculationResponseDTO calculationResponse =
                new PolicyCalculationResponseDTO(saved.getFinalPremiumAmount(), null, null);

        return policyMapper.toResponse(saved, calculationResponse);
    }


    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public Page<PolicyListItemDTO> listPolicies(PolicyListFilterDTO filter, Pageable pageable) {
        log.info("Policies list requested. clientId={} brokerId={} status={} startDate={} endDate={} page={} pageSize={} sort={}",
                filter.clientId(),
                filter.brokerId(),
                filter.status(),
                filter.startDate(),
                filter.endDate(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        Specification<Policy> spec =
                hasClientId(filter.clientId())
                        .and(hasBrokerId(filter.brokerId()))
                        .and(hasStatus(filter.status()))
                        .and(startDateFrom(filter.startDate()))
                        .and(endDateTo(filter.endDate()));

        Page<Policy> page = policyRepository.findAll(spec, pageable);

        log.info("Policies list returned. totalItems={} totalPages={} returnedItems={}",
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements()
        );

        return page.map(policyMapper::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailsResponseDTO getPolicyDetails(Long id) {
        log.info("Policy details requested. policyId={}", id);
        Policy policy = getPolicyDetailsOrThrow(id);
        return policyMapper.toDetailsResponse(policy);
    }

    // --------------------- HELPERS ---------------------

    private Policy getPolicyOrThrow(Long id){
        return policyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Policy with id {} not found for status change", id);
                    return new BusinessException(ErrorCode.POLICY_NOT_FOUND);
                });
    }

    private Policy getPolicyDetailsOrThrow(Long id){
        return policyRepository.findDetailsById(id)
                .orElseThrow(() -> {
                    log.warn("Policy with id {} not found for fetching details", id);
                    return new BusinessException(ErrorCode.POLICY_NOT_FOUND);
                });
    }
}

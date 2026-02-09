package ro.insurance.buildinginsurance.web.dto.policy_dto;

import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.policy_calculation_dto.PolicyCalculationResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PolicyResponseDTO(
        Long id,
        String policyNumber,
        PolicyStatus status,

        Long clientId,
        Long buildingId,
        Long brokerId,

        Long currencyId,
        String currencyCode,

        LocalDate startDate,
        LocalDate endDate,

        BigDecimal basePremiumAmount,
        PolicyCalculationResponseDTO calculationResponse,

        BigDecimal brokerCommissionPct,
        BigDecimal brokerCommissionAmount,

        OffsetDateTime activatedAt,
        OffsetDateTime cancelledAt,
        String cancellationReason
) {}

package ro.insurance.buildinginsurance.web.dto.policyDto;

import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyListItemDTO(
        Long id,
        String policyNumber,
        PolicyStatus status,
        BigDecimal premium,
        String currencyCode,
        String clientName,
        String buildingAddress,
        LocalDate startDate,
        LocalDate endDate
) {}

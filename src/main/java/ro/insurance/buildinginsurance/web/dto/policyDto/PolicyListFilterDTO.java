package ro.insurance.buildinginsurance.web.dto.policyDto;

import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.time.LocalDate;

public record PolicyListFilterDTO(
        Long clientId,
        Long brokerId,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate
) {}

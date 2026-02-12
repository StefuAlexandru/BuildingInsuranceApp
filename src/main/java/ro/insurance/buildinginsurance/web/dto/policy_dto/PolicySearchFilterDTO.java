package ro.insurance.buildinginsurance.web.dto.policy_dto;

import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.time.LocalDate;

public record PolicySearchFilterDTO(
        Long clientId,
        Long brokerId,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate
) { }


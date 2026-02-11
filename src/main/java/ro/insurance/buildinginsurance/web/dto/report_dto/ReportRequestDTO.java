package ro.insurance.buildinginsurance.web.dto.report_dto;

import jakarta.validation.constraints.NotNull;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.time.LocalDate;

public record ReportRequestDTO(
        @NotNull(message = "From date cannot be null")
        LocalDate from,
        @NotNull(message = "To date cannot be null")
        LocalDate to,

        @NotNull(message = "Currency id cannot be null")
        Long currencyId,

        PolicyStatus status,
        BuildingType buildingType
) {}


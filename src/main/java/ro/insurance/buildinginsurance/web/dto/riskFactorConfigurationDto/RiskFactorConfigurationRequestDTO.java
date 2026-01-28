package ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RiskFactorConfigurationRequestDTO(
        @NotNull(message = "Risk factor level cannot be null")
        RiskFactorLevel level,

        // Mandatory when level = CITY/COUNTY/COUNTRY
        Long referenceId,

        // Mandatory when level = BUILDING_TYPE
        BuildingType buildingType,

        @NotNull(message = "Adjustment percentage cannot be null")
        @DecimalMin(value = "-100.0000", message = "Adjustment percentage cannot be less than -100%")
        @DecimalMax(value = "100.0000", message = "Adjustment percentage cannot be greater than 100%")
        BigDecimal adjustmentPercentage,

        @NotNull(message = "Effective date from cannot be null")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) { }

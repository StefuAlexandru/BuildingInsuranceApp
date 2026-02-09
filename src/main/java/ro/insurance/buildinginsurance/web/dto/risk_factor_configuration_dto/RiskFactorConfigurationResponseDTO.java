package ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto;

import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RiskFactorConfigurationResponseDTO(
        Long id,
        RiskFactorLevel level,
        Long referenceId,
        BuildingType buildingType,
        BigDecimal adjustmentPercentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}
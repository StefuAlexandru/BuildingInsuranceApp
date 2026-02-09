package ro.insurance.buildinginsurance.web.dto.fee_configuration_dto;

import ro.insurance.buildinginsurance.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeConfigurationResponseDTO(
        Long id,
        String name,
        FeeType feeType,
        BigDecimal percentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) { }
package ro.insurance.buildinginsurance.web.dto.policyCalculationDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyCalculationRequestDTO(
        @NotNull(message = "Base premium cannot be null")
        @DecimalMin(value = "0.01", message = "Base premium should be greater than zero")
        BigDecimal basePremium,

        @NotNull(message = "Building id cannot be null")
        Long buildingId,

        @NotNull(message = "Currency id cannot be null")
        Long currencyId,

        LocalDate now
) { }

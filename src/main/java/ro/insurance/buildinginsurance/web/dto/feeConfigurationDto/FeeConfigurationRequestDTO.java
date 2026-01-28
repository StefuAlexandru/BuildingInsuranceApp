package ro.insurance.buildinginsurance.web.dto.feeConfigurationDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.insurance.buildinginsurance.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeConfigurationRequestDTO(
        @NotBlank(message = "Fee configuration name cannot be blank")
        @Size(min = 3, max = 120, message = "Fee configuration name length should be greater than 3")
        String name,

        @NotNull(message = "Fee type cannot be null")
        FeeType feeType,

        @NotNull(message = "Percentage cannot be null")
        BigDecimal percentage,

        @NotNull(message = "Effective date from cannot be null")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) { }

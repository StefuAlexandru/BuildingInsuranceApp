package ro.insurance.buildinginsurance.web.dto.policy_dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyCreateRequestDTO(
        @NotNull(message = "Client id cannot be null")
        Long clientId,

        @NotNull(message = "Building id cannot be null")
        Long buildingId,

        @NotNull(message = "Currency id cannot be null")
        Long currencyId,

        @NotNull(message = "Start date of policy cannot be null")
        @FutureOrPresent(message = "Start date of policy cannot be before current date")
        LocalDate startDate,

        @NotNull(message = "End date of policy cannot be null")
        @Future(message = "End date of policy must be in the future")
        LocalDate endDate,

        @NotNull(message = "Base premium amount cannot be null")
        @DecimalMin(value = "0.01", message = "Base premium amount should be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Base premium must have max 12 integer digits and 2 decimals")
        BigDecimal basePremiumAmount
) {}


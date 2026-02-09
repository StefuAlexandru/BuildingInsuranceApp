package ro.insurance.buildinginsurance.web.dto.policy_dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyPatchRequestDTO(
        Long buildingId,

        Long currencyId,

        @FutureOrPresent(message = "Start date of policy cannot be before current date")
        LocalDate startDate,

        @Future(message = "End date must be in the future")
        LocalDate endDate,

        @DecimalMin(value = "0.01", message = "Base premium amount should be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Base premium must have max 12 integer digits and 2 decimals")
        BigDecimal basePremiumAmount
) { }

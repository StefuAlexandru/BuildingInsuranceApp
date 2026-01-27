package ro.insurance.buildinginsurance.web.dto.currencyDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CurrencyRequestDTO(
        @NotBlank(message = "Currency code cannot be blank")
        @Size(min = 3, max = 5, message = "Code length must be between 3 and 5")
        String code,

        @NotBlank(message = "Currency name cannot be blank")
        String name,

        @NotNull(message = "Exchange rate to base cannot be null")
        @DecimalMin(value = "0.000001", inclusive = false, message = "Exchange rate must be > 0")
        BigDecimal exchangeRateToBase
) {
}

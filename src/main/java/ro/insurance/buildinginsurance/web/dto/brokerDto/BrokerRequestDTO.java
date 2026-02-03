package ro.insurance.buildinginsurance.web.dto.brokerDto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BrokerRequestDTO(
        @NotBlank(message = "Broker code cannot be blank")
        @Size(max = 32, message = "Broker code must have max 32 characters")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Broker code must contain only letters, digits, '_' or '-'")
        String brokerCode,

        @NotBlank(message = "Broker name cannot be blank")
        @Size(max = 120, message = "Broker name must have max 120 characters")
        String name,

        @NotBlank(message = "Broker email cannot be blank")
        @Email(message = "Broker email must be a valid email address")
        String email,

        @NotNull(message = "Phone cannot be null")
        @NotBlank(message = "Phone cannot be blank")
        @Pattern(
                regexp = "^(?:\\+40|0)(?:7\\d{8}|[23]\\d{8})$",
                message = "Invalid phone number (ex: 07xxxxxxxx or +40xxxxxxxxx)"
        )
        @Size(max = 50, message = "Phone must be at most 50 characters")
        String phone,

        @DecimalMin(value = "0.00",message = "Commission percentage cannot be negative")
        @DecimalMax(value = "100.00",message = "Commission percentage cannot be greater than 100")
        @Digits(integer = 3, fraction = 2, message = "Commission percentage must have max 3 integer digits and 2 decimals")
        BigDecimal commissionPct
) {}

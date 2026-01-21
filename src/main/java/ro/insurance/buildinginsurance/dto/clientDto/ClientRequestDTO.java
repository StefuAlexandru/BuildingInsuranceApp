package ro.insurance.buildinginsurance.dto.clientDto;

import jakarta.validation.constraints.*;
import ro.insurance.buildinginsurance.enums.ClientType;

public record ClientRequestDTO(
        @NotBlank(message = "Client name cannot be blank")
        @Size(max = 255, message = "Client name must be at most 255 characters")
        String name,

        @NotNull(message = "Client type cannot be null")
        ClientType clientType,

        @NotBlank(message = "Identification number cannot be blank")
        String identificationNumber,

        @NotNull(message = "Email cannot be null")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @NotNull(message = "Phone cannot be null")
        @NotBlank(message = "Phone cannot be blank")
        @Pattern(
                regexp = "^(?:\\+40|0)(?:7\\d{8}|[23]\\d{8})$",
                message = "Invalid phone number (ex: 07xxxxxxxx or +40xxxxxxxxx)"
        )
        @Size(max = 50, message = "Phone must be at most 50 characters")
        String phone,

        Long addressCityId,

        @Size(max = 255, message = "Street must be at most 255 characters")
        String addressStreet,

        @Size(max = 50, message = "Number must be at most 50 characters")
        String addressNumber
) { }

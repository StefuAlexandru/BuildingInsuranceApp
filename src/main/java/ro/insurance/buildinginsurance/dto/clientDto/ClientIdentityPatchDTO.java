package ro.insurance.buildinginsurance.dto.clientDto;

import jakarta.validation.constraints.NotBlank;
import ro.insurance.buildinginsurance.enums.ClientType;

public record ClientIdentityPatchDTO(
        ClientType newClientType,
        String newIdentificationNumber,
        @NotBlank(message = "Reason for changing client identity cannot pe blank")
        String reason
) {}


package ro.insurance.buildinginsurance.web.dto.client_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientLookupRequestDTO(
        @NotBlank(message = "Client name cannot be blank")
        @Size(max = 255, message = "Client name must be at most 255 characters")
        String name,

        @NotBlank(message = "Identification number cannot be blank")
        String identificationNumber
) {
    public ClientLookupRequestDTO {
        name = trimToNull(name);
        identificationNumber = trimToNull(identificationNumber);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

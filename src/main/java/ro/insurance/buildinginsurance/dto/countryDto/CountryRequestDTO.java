package ro.insurance.buildinginsurance.dto.countryDto;

import jakarta.validation.constraints.NotBlank;

public record CountryRequestDTO(
        @NotBlank(message = "Country name cannot be blank")
        String name
) { }

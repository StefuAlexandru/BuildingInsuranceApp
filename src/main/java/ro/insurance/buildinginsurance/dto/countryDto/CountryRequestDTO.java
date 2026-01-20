package ro.insurance.buildinginsurance.dto.countryDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CountryRequestDTO(
        @NotBlank(message = "Country name cannot be blank")
        @Size(min = 1, max = 120, message = "Name length should be greater than 1 and less than 120")
        String name
) { }

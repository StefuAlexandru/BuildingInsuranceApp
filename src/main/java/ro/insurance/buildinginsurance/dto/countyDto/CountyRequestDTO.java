package ro.insurance.buildinginsurance.dto.countyDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CountyRequestDTO(
        @NotBlank(message = "County name cannot be blank")
        String name,
        @NotNull(message = "Country id cannot be null")
        Long countryId
) { }

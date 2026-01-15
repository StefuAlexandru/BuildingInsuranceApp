package ro.insurance.buildinginsurance.dto.cityDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CityRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotNull(message = "County id cannot be null")
        Long countyId
) { }

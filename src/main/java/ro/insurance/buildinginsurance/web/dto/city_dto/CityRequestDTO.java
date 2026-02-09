package ro.insurance.buildinginsurance.web.dto.city_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CityRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 1, max = 120, message = "Name length should be greater than 1 and less than 120")
        String name,
        @NotNull(message = "County id cannot be null")
        Long countyId
) { }

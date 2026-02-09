package ro.insurance.buildinginsurance.web.dto.city_dto;

import ro.insurance.buildinginsurance.web.dto.county_dto.CountyDTO;

public record CityResponseDTO(
        Long id,
        String name,
        CountyDTO county
) { }

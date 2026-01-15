package ro.insurance.buildinginsurance.dto.cityDto;

import ro.insurance.buildinginsurance.dto.countyDto.CountyDTO;

public record CityResponseDTO(
        Long id,
        String name,
        CountyDTO county
) { }

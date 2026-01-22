package ro.insurance.buildinginsurance.web.dto.cityDto;

import ro.insurance.buildinginsurance.web.dto.countyDto.CountyDTO;

public record CityResponseDTO(
        Long id,
        String name,
        CountyDTO county
) { }

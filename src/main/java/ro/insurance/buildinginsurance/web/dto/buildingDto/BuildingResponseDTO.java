package ro.insurance.buildinginsurance.web.dto.buildingDto;

import ro.insurance.buildinginsurance.web.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

public record BuildingResponseDTO(
        Long id,
        CityDTO city,
        String street,
        String number,
        Integer constructionYear,
        BuildingType buildingType
) { }

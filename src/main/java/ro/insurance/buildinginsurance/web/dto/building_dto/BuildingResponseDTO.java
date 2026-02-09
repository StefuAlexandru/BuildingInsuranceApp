package ro.insurance.buildinginsurance.web.dto.building_dto;

import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

public record BuildingResponseDTO(
        Long id,
        CityDTO city,
        String street,
        String number,
        Integer constructionYear,
        BuildingType buildingType
) { }

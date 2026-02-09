package ro.insurance.buildinginsurance.web.dto.building_dto;

import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

import java.math.BigDecimal;

public record BuildingDTO(
        Long id,
        CityDTO city,
        String street,
        String number,
        Integer constructionYear,
        BuildingType buildingType,
        Integer floors,
        BigDecimal surfaceArea,
        BigDecimal insuredValue,
        Boolean floodZone,
        Boolean earthquakeZone
) { }

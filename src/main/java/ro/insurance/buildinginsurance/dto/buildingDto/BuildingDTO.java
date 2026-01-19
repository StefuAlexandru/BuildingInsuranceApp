package ro.insurance.buildinginsurance.dto.buildingDto;

import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.enums.BuildingType;

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

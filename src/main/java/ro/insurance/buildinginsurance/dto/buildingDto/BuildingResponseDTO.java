package ro.insurance.buildinginsurance.dto.buildingDto;

import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.enums.BuildingType;

import java.math.BigDecimal;

public record BuildingResponseDTO(
        Long id,
        CityDTO city,
        String street,
        String number,
        Integer constructionYear,
        BuildingType buildingType
) { }

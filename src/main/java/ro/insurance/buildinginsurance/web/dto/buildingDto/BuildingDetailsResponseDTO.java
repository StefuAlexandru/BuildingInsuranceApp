package ro.insurance.buildinginsurance.web.dto.buildingDto;

import ro.insurance.buildinginsurance.web.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

import java.math.BigDecimal;

public record BuildingDetailsResponseDTO(
        Long id,
        ClientDTO client,
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
) {}
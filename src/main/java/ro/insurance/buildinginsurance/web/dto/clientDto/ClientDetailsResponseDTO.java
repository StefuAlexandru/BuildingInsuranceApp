package ro.insurance.buildinginsurance.web.dto.clientDto;

import ro.insurance.buildinginsurance.web.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.domain.enums.ClientType;

import java.util.List;

public record ClientDetailsResponseDTO(
        Long id,
        ClientType clientType,
        String name,
        String identificationNumber,
        String email,
        String phone,
        CityDTO city,
        String addressStreet,
        String addressNumber,
        List<BuildingDTO> buildings
) {}


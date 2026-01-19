package ro.insurance.buildinginsurance.dto.clientDto;

import ro.insurance.buildinginsurance.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.enums.ClientType;

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


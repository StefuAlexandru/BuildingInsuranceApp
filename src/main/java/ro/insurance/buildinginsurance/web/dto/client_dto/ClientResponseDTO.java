package ro.insurance.buildinginsurance.web.dto.client_dto;

import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.domain.enums.ClientType;

public record ClientResponseDTO(
        Long id,
        ClientType clientType,
        String name,
        String identificationNumber,
        String email,
        String phone,
        CityDTO city,
        String addressStreet,
        String addressNumber
) {}


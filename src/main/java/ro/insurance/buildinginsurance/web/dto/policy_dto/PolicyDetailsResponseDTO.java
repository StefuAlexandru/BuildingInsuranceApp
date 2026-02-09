package ro.insurance.buildinginsurance.web.dto.policy_dto;

import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientDTO;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyDTO;

import java.math.BigDecimal;

public record PolicyDetailsResponseDTO(
    ClientDTO client,
    CityDTO city,
    BuildingDTO building,

    BigDecimal basePremium,
    BigDecimal finalPremium,

    CurrencyDTO currency
){}

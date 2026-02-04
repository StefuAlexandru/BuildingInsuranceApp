package ro.insurance.buildinginsurance.web.dto.policyDto;

import ro.insurance.buildinginsurance.web.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyDTO;

import java.math.BigDecimal;

public record PolicyDetailsResponseDTO(
    ClientDTO client,
    CityDTO city,
    BuildingDTO building,

    BigDecimal basePremium,
    BigDecimal finalPremium,

    CurrencyDTO currency
){}

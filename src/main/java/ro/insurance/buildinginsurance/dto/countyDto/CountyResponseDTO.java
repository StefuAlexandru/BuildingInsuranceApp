package ro.insurance.buildinginsurance.dto.countyDto;

import ro.insurance.buildinginsurance.dto.countryDto.CountryDTO;

public record CountyResponseDTO (
        Long id,
        String name,
        CountryDTO country
) { }

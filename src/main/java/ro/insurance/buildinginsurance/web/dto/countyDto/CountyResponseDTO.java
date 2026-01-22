package ro.insurance.buildinginsurance.web.dto.countyDto;

import ro.insurance.buildinginsurance.web.dto.countryDto.CountryDTO;

public record CountyResponseDTO (
        Long id,
        String name,
        CountryDTO country
) { }

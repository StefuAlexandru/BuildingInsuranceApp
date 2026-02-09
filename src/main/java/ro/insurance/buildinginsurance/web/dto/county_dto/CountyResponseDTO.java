package ro.insurance.buildinginsurance.web.dto.county_dto;

import ro.insurance.buildinginsurance.web.dto.country_dto.CountryDTO;

public record CountyResponseDTO (
        Long id,
        String name,
        CountryDTO country
) { }

package ro.insurance.buildinginsurance.web.dto.county_dto;

public record CountyDTO(
        Long id,
        String name,
        Long countryId,
        String countryName
) {}

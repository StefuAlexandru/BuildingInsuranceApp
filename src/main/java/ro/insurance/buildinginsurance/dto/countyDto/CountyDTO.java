package ro.insurance.buildinginsurance.dto.countyDto;

public record CountyDTO(
        Long id,
        String name,
        Long countryId,
        String countryName
) {}

package ro.insurance.buildinginsurance.web.dto.countyDto;

public record CountyDTO(
        Long id,
        String name,
        Long countryId,
        String countryName
) {}

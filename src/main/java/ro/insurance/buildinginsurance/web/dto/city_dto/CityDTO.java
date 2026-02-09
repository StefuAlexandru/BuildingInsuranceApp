package ro.insurance.buildinginsurance.web.dto.city_dto;

public record CityDTO(
        Long id,
        String name,
        String countyName,
        String countryName
){}

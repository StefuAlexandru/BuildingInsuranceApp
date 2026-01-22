package ro.insurance.buildinginsurance.web.dto.cityDto;

public record CityDTO(
        Long id,
        String name,
        String countyName,
        String countryName
){}

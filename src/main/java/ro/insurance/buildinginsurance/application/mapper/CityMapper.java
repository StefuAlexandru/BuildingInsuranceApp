package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.*;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.County;

@Mapper(
        componentModel = "spring",
        uses = {CountyMapper.class}
)
public interface CityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "cityRequestDTO.name")
    @Mapping(target = "county", source = "county")
    City toEntity(CityRequestDTO cityRequestDTO, County county);


    CityResponseDTO toResponse(City client);

    @Mapping(target = "countyName", source = "county.name")
    @Mapping(target = "countryName", source = "county.country.name")
    CityDTO toDTO(City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "cityRequestDTO.name")
    @Mapping(target = "county", source = "county")
    void updateFromDTO(CityRequestDTO cityRequestDTO, County county,@MappingTarget City city);

    @AfterMapping
    default void normalize(@MappingTarget City city) {
        city.setName(trimToNull(city.getName()));
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.*;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.County;

import static ro.insurance.buildinginsurance.util.StringNormalizer.trimToNull;

@Mapper(
        componentModel = "spring",
        uses = {CountyMapper.class}
)
public interface CityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "cityRequestDTO.name")
    @Mapping(target = "county", source = "county")
    City toEntity(CityRequestDTO cityRequestDTO, County county);

    CityResponseDTO toResponse(City city);

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
}

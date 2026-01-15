package ro.insurance.buildinginsurance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.County;

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
}

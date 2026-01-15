package ro.insurance.buildinginsurance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.dto.countyDto.CountyDTO;
import ro.insurance.buildinginsurance.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.entity.Country;
import ro.insurance.buildinginsurance.entity.County;

@Mapper(
        componentModel = "spring",
        uses = {CountryMapper.class}
)
public interface CountyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "countyRequestDTO.name")
    @Mapping(target = "country", source = "country")
    County toEntity(CountyRequestDTO countyRequestDTO, Country country);

    CountyResponseDTO toResponse(County county);

    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "countryName", source = "country.name")
    CountyDTO toDTO(County county);
}


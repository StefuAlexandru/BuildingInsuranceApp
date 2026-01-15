package ro.insurance.buildinginsurance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ro.insurance.buildinginsurance.dto.countryDto.CountryDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.entity.Country;

@Mapper(componentModel = "spring")
public interface CountryMapper {
    @Mapping(target = "id", ignore = true)
    Country toEntity(CountryRequestDTO countryRequestDTO);

    CountryResponseDTO toResponse(Country country);

    CountryDTO toDTO(Country country);
}

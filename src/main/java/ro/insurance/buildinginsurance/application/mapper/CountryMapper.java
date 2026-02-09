package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryDTO;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;

import static ro.insurance.buildinginsurance.util.StringNormalizer.trimToNull;

@Mapper(componentModel = "spring")
public interface CountryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "countryRequestDTO.name")
    Country toEntity(CountryRequestDTO countryRequestDTO);

    CountryResponseDTO toResponse(Country country);

    CountryDTO toDTO(Country country);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "countryRequestDTO.name")
    void updateFromDTO(CountryRequestDTO countryRequestDTO, @MappingTarget Country country);

    @AfterMapping
    default void normalize(@MappingTarget Country country) {
        country.setName(trimToNull(country.getName()));
    }
}

package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;

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

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

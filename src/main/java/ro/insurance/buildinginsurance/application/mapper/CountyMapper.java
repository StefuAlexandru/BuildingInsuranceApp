package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.entity.County;

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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "countyRequestDTO.name")
    @Mapping(target = "country", source = "country")
    void updateFromDTO(CountyRequestDTO countyRequestDTO, Country country, @MappingTarget County county);

    @AfterMapping
    default void normalize(@MappingTarget County county) {
        county.setName(trimToNull(county.getName()));
    }
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}


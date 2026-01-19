package ro.insurance.buildinginsurance.mapper;


import org.mapstruct.*;
import ro.insurance.buildinginsurance.dto.buildingDto.*;
import ro.insurance.buildinginsurance.entity.Building;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;

@Mapper(
        componentModel = "spring",
        uses = {ClientMapper.class,CityMapper.class}
)
public interface BuildingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "city", source = "city")
    Building toEntity(BuildingRequestDTO buildingRequestDTO, Client client, City city);

    BuildingDTO toDTO(Building building);
    BuildingDetailsResponseDTO toDetailsResponse(Building building);
    BuildingResponseDTO toResponse(Building building);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "city", source = "city")
    void updateFromDto(BuildingUpdateRequestDTO dto, City city, @MappingTarget Building building);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "street", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "city", source = "city")
    void patchFromDto(BuildingPatchDTO dto, City city, @MappingTarget Building building);


    @AfterMapping
    default void normalizeAfterCreate(BuildingRequestDTO buildingRequestDTO, @MappingTarget Building building) {
        building.setStreet(trimToNull(building.getStreet()));
        building.setNumber(trimToNull(building.getNumber()));
    }

    @AfterMapping
    default void normalizeAfterUpdate(BuildingUpdateRequestDTO buildingUpdateRequestDTO, @MappingTarget Building building) {
        building.setStreet(trimToNull(building.getStreet()));
        building.setNumber(trimToNull(building.getNumber()));
    }

    @AfterMapping
    default void applyPatchStrings(BuildingPatchDTO buildingPatchDTO, @MappingTarget Building building) {
        if (buildingPatchDTO.street() != null) {
            String s = trimToNull(buildingPatchDTO.street());
            if (s != null) building.setStreet(s);
        }
        if (buildingPatchDTO.number() != null) {
            String n = trimToNull(buildingPatchDTO.number());
            if (n != null) building.setNumber(n);
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

}

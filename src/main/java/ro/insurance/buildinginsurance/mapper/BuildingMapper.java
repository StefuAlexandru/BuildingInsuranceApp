package ro.insurance.buildinginsurance.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingResponseDTO;
import ro.insurance.buildinginsurance.entity.Building;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;

@Mapper(
        componentModel = "spring",
        uses = {ClientMapper.class,CityMapper.class}
)
public interface BuildingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client" , expression = "java(client)")
    @Mapping(target = "city" , expression = "java(city)")
    Building toEntity(BuildingRequestDTO buildingRequestDTO, Client client, City city);

    BuildingResponseDTO toResponse(Building building);

    BuildingDTO toDTO(Building building);
}

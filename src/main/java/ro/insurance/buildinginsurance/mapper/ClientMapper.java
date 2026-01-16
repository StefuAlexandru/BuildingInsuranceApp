package ro.insurance.buildinginsurance.mapper;

import org.mapstruct.*;
import ro.insurance.buildinginsurance.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;

@Mapper(
        componentModel = "spring",
        uses = {CityMapper.class}
)
public interface ClientMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "clientRequestDTO.name")
    @Mapping(target = "identificationNumber", source = "clientRequestDTO.identificationNumber")
    @Mapping(target = "email", source = "clientRequestDTO.email")
    @Mapping(target = "phone", source = "clientRequestDTO.phone")
    @Mapping(target = "addressStreet", source = "clientRequestDTO.addressStreet")
    @Mapping(target = "addressNumber", source = "clientRequestDTO.addressNumber")
    @Mapping(target = "addressCity", source = "city")
    @Mapping(target = "clientType", expression = "java(ClientType.valueOf(clientRequestDTO.clientType()))")
    @Mapping(target = "buildings", ignore = true)
    Client toEntity(ClientRequestDTO clientRequestDTO, City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identificationNumber", ignore = true)
    @Mapping(target = "clientType", ignore = true)
    @Mapping(target = "buildings", ignore = true)
    @Mapping(target = "addressCity", source = "city")
    @Mapping(target = "name", source = "clientUpdateRequestDTO.name")
    void updateFromDto(ClientUpdateRequestDTO clientUpdateRequestDTO, City city, @MappingTarget Client client);

    @Mapping(target = "city", source = "addressCity")
    ClientResponseDTO toResponse(Client client);

    @AfterMapping
    default void normalize(@MappingTarget Client client) {
        client.setName(trim(client.getName()));
        client.setEmail(trim(client.getEmail()));
        client.setPhone(trim(client.getPhone()));

        client.setAddressStreet(trimToNull(client.getAddressStreet()));
        client.setAddressNumber(trimToNull(client.getAddressNumber()));
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

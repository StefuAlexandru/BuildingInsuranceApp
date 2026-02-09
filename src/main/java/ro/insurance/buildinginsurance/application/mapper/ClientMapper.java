package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.*;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientDetailsResponseDTO;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientRequestDTO;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientResponseDTO;
import ro.insurance.buildinginsurance.web.dto.client_dto.ClientUpdateRequestDTO;

import static ro.insurance.buildinginsurance.util.StringNormalizer.trimToNull;

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
    @Mapping(target = "clientType", source = "clientRequestDTO.clientType")
    @Mapping(target = "buildings", ignore = true)
    Client toEntity(ClientRequestDTO clientRequestDTO, City city);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identificationNumber", ignore = true)
    @Mapping(target = "clientType", ignore = true)
    @Mapping(target = "buildings", ignore = true)
    @Mapping(target = "addressCity", source = "city")
    @Mapping(target = "name", source = "clientUpdateRequestDTO.name")
    void updateFromDto(ClientUpdateRequestDTO clientUpdateRequestDTO, City city, @MappingTarget Client client);

    @Mapping(target = "city", source = "addressCity")
    ClientResponseDTO toResponse(Client client);

    @Mapping(target = "city", source = "addressCity")
    ClientDetailsResponseDTO toDetailsResponse(Client client);

    @AfterMapping
    default void normalize(@MappingTarget Client client) {
        client.setName(trim(client.getName()));
        client.setEmail(trim(client.getEmail()));
        client.setPhone(trim(client.getPhone()));

        client.setIdentificationNumber(trim(client.getIdentificationNumber()));

        client.setAddressStreet(trimToNull(client.getAddressStreet()));
        client.setAddressNumber(trimToNull(client.getAddressNumber()));
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}

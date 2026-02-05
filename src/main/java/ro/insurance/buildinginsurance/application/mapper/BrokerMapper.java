package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.domain.entity.Broker;
import ro.insurance.buildinginsurance.util.StringNormalizer;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerResponseDTO;

@Mapper(componentModel = "spring")
public interface BrokerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Broker toEntity(BrokerRequestDTO brokerRequestDTO);

    BrokerResponseDTO toResponse(Broker broker);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateFromDto(BrokerRequestDTO brokerRequestDTO, @MappingTarget Broker broker);

    @AfterMapping
    default void normalize(@MappingTarget Broker broker, BrokerRequestDTO dto) {
        broker.setBrokerCode(StringNormalizer.normalizeUpper(dto.brokerCode()));
        broker.setName(StringNormalizer.trimToNull(dto.name()));
        broker.setEmail(StringNormalizer.trimToNull(dto.email()));
        broker.setPhone(StringNormalizer.trimToNull(dto.phone()));
    }
}

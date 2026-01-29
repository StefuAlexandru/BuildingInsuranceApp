package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationResponseDTO;

@Mapper(componentModel = "spring")
public interface RiskFactorConfigurationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    RiskFactorConfiguration toEntity(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO);

    @Mapping(target = "isActive", source = "riskFactorConfiguration.active")
    RiskFactorConfigurationResponseDTO toResponse(RiskFactorConfiguration riskFactorConfiguration);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateFromDTO(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO, @MappingTarget RiskFactorConfiguration riskFactorConfiguration);
}

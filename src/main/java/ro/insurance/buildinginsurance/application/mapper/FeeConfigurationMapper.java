package ro.insurance.buildinginsurance.application.mapper;


import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationResponseDTO;

import static ro.insurance.buildinginsurance.util.StringNormalizer.trimToNull;

@Mapper(componentModel = "spring")
public interface FeeConfigurationMapper {

    @Mapping(target = "id", ignore = true)
    FeeConfiguration toEntity(FeeConfigurationRequestDTO feeConfigurationRequestDTO);

    FeeConfigurationResponseDTO toResponse(FeeConfiguration feeConfiguration);

    @Mapping(target = "id", ignore = true)
    void updateFromDTO(FeeConfigurationRequestDTO feeConfigurationRequestDTO, @MappingTarget FeeConfiguration feeConfiguration);

    @AfterMapping
    default void normalize(@MappingTarget FeeConfiguration feeConfiguration) {
        feeConfiguration.setName(trimToNull(feeConfiguration.getName()));
    }

}

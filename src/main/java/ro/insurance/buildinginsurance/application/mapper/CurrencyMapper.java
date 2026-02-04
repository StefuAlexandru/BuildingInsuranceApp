package ro.insurance.buildinginsurance.application.mapper;


import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyResponseDTO;

import static ro.insurance.buildinginsurance.util.StringNormalizer.normalizeUpper;
import static ro.insurance.buildinginsurance.util.StringNormalizer.trimToNull;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Currency toEntity(CurrencyRequestDTO currencyRequestDTO);

    @Mapping(target = "isActive", source = "currency.active")
    CurrencyResponseDTO toResponse(Currency currency);

    CurrencyDTO toDto(Currency currency);

    @AfterMapping
    default void normalize(@MappingTarget Currency currency) {
        currency.setCode(normalizeUpper(currency.getCode()));
        currency.setName(trimToNull(currency.getName()));
    }
}

package ro.insurance.buildinginsurance.web.dto.currency_dto;

import java.math.BigDecimal;

public record CurrencyResponseDTO(
        Long id,
        String code,
        String name,
        BigDecimal exchangeRateToBase,
        boolean isActive
) {}

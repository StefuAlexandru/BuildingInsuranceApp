package ro.insurance.buildinginsurance.web.dto.currencyDto;

import java.math.BigDecimal;

public record CurrencyResponseDTO(
        Long id,
        String code,
        String name,
        BigDecimal exchangeRateToBase,
        boolean isActive
) {}

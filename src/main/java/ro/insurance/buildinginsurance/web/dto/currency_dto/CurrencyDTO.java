package ro.insurance.buildinginsurance.web.dto.currency_dto;

import java.math.BigDecimal;

public record CurrencyDTO(
        Long id,
        String code,
        BigDecimal exchangeRateToBase
) { }

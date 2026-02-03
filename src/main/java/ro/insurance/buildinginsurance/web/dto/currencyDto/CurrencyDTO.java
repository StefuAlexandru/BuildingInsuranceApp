package ro.insurance.buildinginsurance.web.dto.currencyDto;

import java.math.BigDecimal;

public record CurrencyDTO(
        Long id,
        String code,
        BigDecimal exchangeRateToBase
) { }

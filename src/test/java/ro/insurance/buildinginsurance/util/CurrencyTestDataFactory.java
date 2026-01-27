package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyResponseDTO;

import java.math.BigDecimal;

public class CurrencyTestDataFactory {

    private CurrencyTestDataFactory() {}

    // ------------------- DTOS -------------------

    public static CurrencyRequestDTO aCurrencyRequest() {
        return new CurrencyRequestDTO(" RON ", "Romanian Leu", new BigDecimal("1.000000"));
    }

    public static CurrencyRequestDTO aCurrencyRequestWithCode(String code) {
        CurrencyRequestDTO base = aCurrencyRequest();
        return new CurrencyRequestDTO(code, base.name(), base.exchangeRateToBase());
    }

    public static CurrencyRequestDTO aCurrencyRequest(String code, String name, BigDecimal rate) {
        return new CurrencyRequestDTO(code, name, rate);
    }

    public static CurrencyResponseDTO aCurrencyResponse() {
        return new CurrencyResponseDTO(1L, "RON", "Romanian Leu", new BigDecimal("1.000000"), true);
    }

    public static CurrencyResponseDTO aCurrencyResponseWithIdAndActive(Long id, boolean active) {
        CurrencyResponseDTO base = aCurrencyResponse();
        return new CurrencyResponseDTO(id, base.code(), base.name(), base.exchangeRateToBase(), active);
    }

    public static CurrencyResponseDTO aCurrencyResponse(Long id, String code, String name, BigDecimal rate, boolean active) {
        return new CurrencyResponseDTO(id, code, name, rate, active);
    }

    // ------------------- ENTITY -------------------

    public static Currency aCurrency() {
        return aCurrencyWithId(1L);
    }

    public static Currency aCurrencyWithoutId(){
        Currency c = new Currency();
        c.setCode("RON");
        c.setName("Romanian Leu");
        c.setExchangeRateToBase(new BigDecimal("1.000000"));
        c.setActive(true);
        return c;
    }

    public static Currency aCurrencyWithId(Long id) {
        Currency c = new Currency();
        c.setId(id);
        c.setCode("RON");
        c.setName("Romanian Leu");
        c.setExchangeRateToBase(new BigDecimal("1.000000"));
        c.setActive(true);
        return c;
    }

    public static Currency aCurrencyWithIdCodeAndActive(Long id, String code, boolean active) {
        Currency c = aCurrencyWithId(id);
        c.setCode(code);
        c.setActive(active);
        return c;
    }

    public static Currency aCurrencyEntity(Long id, String code, String name, BigDecimal rate, boolean active) {
        Currency c = new Currency();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        c.setExchangeRateToBase(rate);
        c.setActive(active);
        return c;
    }
}

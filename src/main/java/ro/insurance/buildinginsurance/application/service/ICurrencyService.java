package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyResponseDTO;

public interface ICurrencyService {
    Page<CurrencyResponseDTO> getAllCurrencies(Pageable pageable);
    CurrencyResponseDTO registerCurrency(CurrencyRequestDTO currencyRequestDTO);
    CurrencyResponseDTO activate(Long currencyId);
    CurrencyResponseDTO deactivate(Long currencyId);
}

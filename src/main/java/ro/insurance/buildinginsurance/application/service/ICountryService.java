package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryResponseDTO;

public interface ICountryService {
    CountryResponseDTO registerCountry(CountryRequestDTO countryRequestDTO);

    CountryResponseDTO getCountryById(Long countryId);
    Page<CountryResponseDTO> getCountryByPartialName(String countyName, Pageable pageable);

    CountryResponseDTO updateCountry(Long countryId,CountryRequestDTO countryRequestDTO);

    void deleteCountry(Long countryId);
}

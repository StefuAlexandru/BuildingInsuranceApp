package ro.insurance.buildinginsurance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.dto.countryDto.*;

public interface ICountryService {
    CountryResponseDTO registerCountry(CountryRequestDTO countryRequestDTO);

    CountryResponseDTO getCountryById(Long countryId);
    Page<CountryResponseDTO> getCountryByPartialName(String countyName, Pageable pageable);

    CountryResponseDTO updateCountry(Long countryId,CountryRequestDTO countryRequestDTO);

    void deleteCountry(Long countryId);
}

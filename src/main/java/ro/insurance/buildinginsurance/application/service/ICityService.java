package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityResponseDTO;

public interface ICityService {
    CityResponseDTO registerCity(CityRequestDTO cityRequestDTO);

    CityResponseDTO getCityById(Long cityId);
    Page<CityResponseDTO> getCityByPartialName(String cityName, Pageable pageable);

    CityResponseDTO updateCity(Long cityId,CityRequestDTO cityRequestDTO);

    void deleteCity(Long cityId);
}

package ro.insurance.buildinginsurance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.dto.countyDto.*;

public interface ICountyService {
    CountyResponseDTO registerCounty(CountyRequestDTO countyRequestDTO);

    CountyResponseDTO getCountyById(Long countyId);
    Page<CountyResponseDTO> getCountyByPartialName(String countyName, Pageable pageable);

    CountyResponseDTO updateCounty(Long countyId,CountyRequestDTO countyRequestDTO);

    void deleteCounty(Long countyId);
}

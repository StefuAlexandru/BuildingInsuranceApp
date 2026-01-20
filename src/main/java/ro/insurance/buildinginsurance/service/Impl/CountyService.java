package ro.insurance.buildinginsurance.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.entity.County;
import ro.insurance.buildinginsurance.entity.Country;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.mapper.CountyMapper;
import ro.insurance.buildinginsurance.repository.CountyRepository;
import ro.insurance.buildinginsurance.repository.CountryRepository;
import ro.insurance.buildinginsurance.service.ICountyService;

@Service
@RequiredArgsConstructor
@Transactional
public class CountyService implements ICountyService {

    private final CountyRepository countyRepository;
    private final CountyMapper countyMapper;
    private final CountryRepository countryRepository;

    // --------------------- CREATE ---------------------

    @Override
    public CountyResponseDTO registerCounty(CountyRequestDTO countyRequestDTO) {
        String trimmedName = countyRequestDTO.name().trim();
        validateCountyOrThrow(trimmedName, countyRequestDTO.countryId());

        Country country = findCountryOrThrow(countyRequestDTO.countryId());
        County county = countyMapper.toEntity(countyRequestDTO, country);

        County saved = countyRepository.save(county);
        return countyMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public CountyResponseDTO getCountyById(Long countyId) {
        County county = findCountyOrThrow(countyId);
        return countyMapper.toResponse(county);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountyResponseDTO> getCountyByPartialName(String countyName, Pageable pageable) {
        String name = (countyName == null) ? "" : countyName;
        return countyRepository.getCountyByPartialName(name, pageable)
                .map(countyMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public CountyResponseDTO updateCounty(Long countyId, CountyRequestDTO countyRequestDTO) {
        County county = findCountyOrThrow(countyId);

        String trimmedName = countyRequestDTO.name().trim();
        validateCountyOrThrow(trimmedName, countyRequestDTO.countryId());

        Country country = findCountryOrThrow(countyRequestDTO.countryId());

        countyMapper.updateFromDTO(countyRequestDTO, country, county);

        County saved = countyRepository.save(county);
        return countyMapper.toResponse(saved);
    }

    // --------------------- DELETE ---------------------

    @Override
    public void deleteCounty(Long countyId) {
        try {
            countyRepository.deleteById(countyId);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.COUNTY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.COUNTY_HAS_DEPENDENCIES);
        }
    }

    // --------------------- HELPERS ---------------------

    private void validateCountyOrThrow(String countyName, Long countryId) {
        if (countyRepository.existsByNameIgnoreCaseAndCountryId(countyName, countryId)) {
            throw new BusinessException(ErrorCode.COUNTY_ALREADY_EXIST);
        }
    }

    private Country findCountryOrThrow(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private County findCountyOrThrow(Long countyId) {
        return countyRepository.findById(countyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTY_NOT_FOUND));
    }
}

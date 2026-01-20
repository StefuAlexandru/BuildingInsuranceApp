package ro.insurance.buildinginsurance.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.entity.Country;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.mapper.CountryMapper;
import ro.insurance.buildinginsurance.repository.CountryRepository;
import ro.insurance.buildinginsurance.service.ICountryService;

@Service
@RequiredArgsConstructor
@Transactional
public class CountryService implements ICountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    // --------------------- CREATE ---------------------

    @Override
    public CountryResponseDTO registerCountry(CountryRequestDTO countryRequestDTO) {
        String trimmedName = countryRequestDTO.name().trim();
        validateCountryOrThrow(trimmedName);

        Country country = countryMapper.toEntity(countryRequestDTO);

        Country saved = countryRepository.save(country);
        return countryMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public CountryResponseDTO getCountryById(Long countryId) {
        Country country = findCountryOrThrow(countryId);
        return countryMapper.toResponse(country);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponseDTO> getCountryByPartialName(String countryName, Pageable pageable) {
        String name = (countryName == null) ? "" : countryName;
        return countryRepository.getCountryByPartialName(name, pageable)
                .map(countryMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public CountryResponseDTO updateCountry(Long countryId, CountryRequestDTO countryRequestDTO) {
        Country country = findCountryOrThrow(countryId);

        String trimmedName = countryRequestDTO.name().trim();
        validateCountryOrThrow(trimmedName);

        countryMapper.updateFromDTO(countryRequestDTO, country);

        Country saved = countryRepository.save(country);
        return countryMapper.toResponse(saved);
    }

    // --------------------- DELETE ---------------------

    @Override
    public void deleteCountry(Long countryId) {
        try {
            countryRepository.deleteById(countryId);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.COUNTRY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.COUNTRY_HAS_DEPENDENCIES);
        }
    }

    // --------------------- HELPERS ---------------------

    private void validateCountryOrThrow(String countryName) {
        if (countryRepository.existsByNameIgnoreCase(countryName)) {
            throw new BusinessException(ErrorCode.COUNTRY_ALREADY_EXIST);
        }
    }

    private Country findCountryOrThrow(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }
}

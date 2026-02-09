package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CountryMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.application.service.ICountryService;

@Slf4j
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
        log.info("Register country requested: name='{}'", trimmedName);

        validateCountryOrThrow(trimmedName);

        Country country = countryMapper.toEntity(countryRequestDTO);
        Country saved = countryRepository.save(country);

        log.info("Country registered: id={}, name='{}'", saved.getId(), saved.getName());
        return countryMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public CountryResponseDTO getCountryById(Long countryId) {
        log.info("Get country by id: {}", countryId);
        Country country = findCountryOrThrow(countryId);
        return countryMapper.toResponse(country);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountryResponseDTO> getCountryByPartialName(String countryName, Pageable pageable) {
        String name = (countryName == null) ? "" : countryName;
        log.info("Search countries: query='{}', page={}, size={}, sort={}",
                name, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return countryRepository.getCountryByPartialName(name, pageable)
                .map(countryMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public CountryResponseDTO updateCountry(Long countryId, CountryRequestDTO dto) {
        String trimmedName = dto.name() == null ? null : dto.name().trim();
        log.info("Update country requested: id={}, newName='{}'", countryId, trimmedName);

        Country country = findCountryOrThrow(countryId);
        validateCountryOrThrow(trimmedName);

        String oldName = country.getName();

        countryMapper.updateFromDTO(dto, country);

        Country saved = countryRepository.save(country);

        log.info("Country updated: id={}, name '{}' -> '{}'", saved.getId(), oldName, saved.getName());

        return countryMapper.toResponse(saved);
    }

    // --------------------- DELETE ---------------------

    @Override
    public void deleteCountry(Long countryId) {
        log.warn("Delete country requested: id={}", countryId);
        try {
            countryRepository.deleteById(countryId);
            log.info("Country deleted: id={}", countryId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete country failed - not found: id={}", countryId);
            throw new BusinessException(ErrorCode.COUNTRY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete country failed - has dependencies: id={}", countryId);
            throw new BusinessException(ErrorCode.COUNTRY_HAS_DEPENDENCIES);
        }
    }

    // --------------------- HELPERS ---------------------

    private void validateCountryOrThrow(String countryName) {
        if (countryRepository.existsByNameIgnoreCase(countryName)) {
            log.warn("Country already exists: name='{}'", countryName);
            throw new BusinessException(ErrorCode.COUNTRY_ALREADY_EXIST);
        }
    }

    private Country findCountryOrThrow(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> {
                    log.warn("Country not found: id={}", countryId);
                    return new BusinessException(ErrorCode.COUNTRY_NOT_FOUND);
                });
    }
}

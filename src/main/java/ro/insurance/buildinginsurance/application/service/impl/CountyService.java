package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CountyMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.application.service.ICountyService;

@Slf4j
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
        log.info("Register county requested: name='{}', countryId={}", trimmedName, countyRequestDTO.countryId());

        validateCountyOrThrow(trimmedName, countyRequestDTO.countryId());

        Country country = findCountryOrThrow(countyRequestDTO.countryId());
        County county = countyMapper.toEntity(countyRequestDTO, country);
        County saved = countyRepository.save(county);

        log.info("County registered: id={}, name='{}', countryId={}", saved.getId(), saved.getName(), country.getId());
        return countyMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public CountyResponseDTO getCountyById(Long countyId) {
        log.info("Get county by id: {}", countyId);
        County county = findCountyOrThrow(countyId);
        return countyMapper.toResponse(county);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CountyResponseDTO> getCountyByPartialName(String countyName, Pageable pageable) {
        String name = (countyName == null) ? "" : countyName;
        log.info("Search counties: query='{}', page={}, size={}, sort={}",
                name, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        return countyRepository.getCountyByPartialName(name, pageable)
                .map(countyMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public CountyResponseDTO updateCounty(Long countyId, CountyRequestDTO dto) {
        String trimmedName = dto.name() == null ? null : dto.name().trim();
        log.info("Update county requested: id={}, newName='{}', newCountryId={}", countyId, trimmedName, dto.countryId());

        County county = findCountyOrThrow(countyId);

        validateCountyOrThrow(trimmedName, dto.countryId());
        Country country = findCountryOrThrow(dto.countryId());

        Long oldCountryId = county.getCountry() != null ? county.getCountry().getId() : null;
        String oldName = county.getName();

        countyMapper.updateFromDTO(dto, country, county);

        County saved = countyRepository.save(county);

        log.info("County updated: id={}, name '{}' -> '{}', countryId {} -> {}",
                saved.getId(), oldName, saved.getName(), oldCountryId, country.getId());

        return countyMapper.toResponse(saved);
    }

    // --------------------- DELETE ---------------------

    @Override
    public void deleteCounty(Long countyId) {
        log.warn("Delete county requested: id={}", countyId);
        try {
            countyRepository.deleteById(countyId);
            log.info("County deleted: id={}", countyId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete county failed - not found: id={}", countyId);
            throw new BusinessException(ErrorCode.COUNTY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete county failed - has dependencies: id={}", countyId);
            throw new BusinessException(ErrorCode.COUNTY_HAS_DEPENDENCIES);
        }
    }

    // --------------------- HELPERS ---------------------

    private void validateCountyOrThrow(String countyName, Long countryId) {
        if (countyRepository.existsByNameIgnoreCaseAndCountryId(countyName, countryId)) {
            log.warn("County already exists: name='{}', countryId={}", countyName, countryId);
            throw new BusinessException(ErrorCode.COUNTY_ALREADY_EXIST);
        }
    }

    private Country findCountryOrThrow(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> {
                    log.warn("Country not found: id={}", countryId);
                    return new BusinessException(ErrorCode.COUNTRY_NOT_FOUND);
                });
    }

    private County findCountyOrThrow(Long countyId) {
        return countyRepository.findById(countyId)
                .orElseThrow(() -> {
                    log.warn("County not found: id={}", countyId);
                    return new BusinessException(ErrorCode.COUNTY_NOT_FOUND);
                });
    }
}

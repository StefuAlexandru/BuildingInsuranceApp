package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CityMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.application.service.ICityService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CityService implements ICityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    private final CountyRepository countyRepository;

    // ---------------------  CREATE  ---------------------

    @Override
    public CityResponseDTO registerCity(CityRequestDTO cityRequestDTO) {
        String trimmedName = cityRequestDTO.name().trim();
        log.info("Register city: name='{}', countyId={}", trimmedName, cityRequestDTO.countyId());
        validateCityOrThrow(trimmedName, cityRequestDTO.countyId());

        County county = findCountyOrThrow(cityRequestDTO.countyId());

        City city = cityMapper.toEntity(cityRequestDTO,county);

        City saved = cityRepository.save(city);
        log.info("City registered: id={}, name='{}', countyId={}", saved.getId(), saved.getName(), county.getId());
        return cityMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    public CityResponseDTO getCityById(Long cityId) {
        log.info("Get city by id: {}", cityId);
        City city = findCityOrThrow(cityId);
        return cityMapper.toResponse(city);
    }

    @Override
    public Page<CityResponseDTO> getCityByPartialName(String cityName, Pageable pageable) {
        String query = (cityName == null) ? "" : cityName;
        log.info("Search cities: query='{}', page={}, size={}, sort={}", query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return cityRepository.getCityByPartialName(cityName, pageable)
                .map(cityMapper::toResponse);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public CityResponseDTO updateCity(Long cityId,CityRequestDTO cityRequestDTO) {
        log.info("Update city: id={}, newName='{}', newCountyId={}", cityId, cityRequestDTO.name(), cityRequestDTO.countyId());
        City city = findCityOrThrow(cityId);
        County county = findCountyOrThrow(cityRequestDTO.countyId());

        Long oldCountyId = city.getCounty() != null ? city.getCounty().getId() : null;
        String oldName = city.getName();

        cityMapper.updateFromDTO(cityRequestDTO,county,city);

        City saved = cityRepository.save(city);
        log.info("City updated: id={}, name '{}' -> '{}', countyId {} -> {}", saved.getId(), oldName, saved.getName(), oldCountyId, county.getId());
        return cityMapper.toResponse(saved);
    }

    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteCity(Long cityId) {
        log.warn("Delete city requested: id={}", cityId);
        try {
            cityRepository.deleteById(cityId);
            log.info("City deleted: id={}", cityId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete city failed - not found: id={}", cityId);
            throw new BusinessException(ErrorCode.CITY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete city failed - has dependencies: id={}", cityId);
            throw new BusinessException(ErrorCode.CITY_HAS_DEPENDENCIES);
        }
    }

    // ---------------------  HELPERS  ---------------------

    private void validateCityOrThrow(String cityName, Long countyId) {
        if (cityRepository.existsByNameIgnoreCaseAndCountyId(cityName, countyId)) {
            log.warn("City already exists: name='{}', countyId={}", cityName, countyId);
            throw new BusinessException(ErrorCode.CITY_ALREADY_EXIST);
        }
    }

    private County findCountyOrThrow(Long countyId) {
        return countyRepository.findById(countyId)
                .orElseThrow(() -> {
                    log.warn("County not found: id={}", countyId);
                    return new BusinessException(ErrorCode.COUNTY_NOT_FOUND);
                });
    }

    private City findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> {
                    log.warn("City not found: id={}", cityId);
                    return new BusinessException(ErrorCode.CITY_NOT_FOUND);
                });
    }

}

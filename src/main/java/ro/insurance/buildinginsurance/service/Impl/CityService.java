package ro.insurance.buildinginsurance.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.County;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.mapper.CityMapper;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.CountyRepository;
import ro.insurance.buildinginsurance.service.ICityService;

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
        validateCityOrThrow(trimmedName, cityRequestDTO.countyId());

        County county = findCountyOrThrow(cityRequestDTO.countyId());

        City city = cityMapper.toEntity(cityRequestDTO,county);

        City saved = cityRepository.save(city);
        return cityMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    public CityResponseDTO getCityById(Long cityId) {
        City city = findCityOrThrow(cityId);
        return cityMapper.toResponse(city);
    }

    @Override
    public Page<CityResponseDTO> getCityByPartialName(String cityName, Pageable pageable) {
        return cityRepository.getCityByPartialName(cityName, pageable)
                .map(cityMapper::toResponse);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public CityResponseDTO updateCity(Long cityId,CityRequestDTO cityRequestDTO) {
        City city = findCityOrThrow(cityId);
        County county = findCountyOrThrow(cityRequestDTO.countyId());

        cityMapper.updateFromDTO(cityRequestDTO,county,city);

        City saved = cityRepository.save(city);
        return cityMapper.toResponse(saved);
    }

    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteCity(Long cityId) {
        try {
            cityRepository.deleteById(cityId);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.CITY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CITY_HAS_DEPENDENCIES);
        }
    }

    // ---------------------  HELPERS  ---------------------

    private void validateCityOrThrow(String cityName,Long countyId) {
        if(cityRepository.existsByNameIgnoreCaseAndCountyId(cityName,countyId)){
            throw new BusinessException(ErrorCode.CITY_ALREADY_EXIST);
        }
    }

    private County findCountyOrThrow(Long countyId) {
        return countyRepository.findById(countyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUNTY_NOT_FOUND));
    }

    private City findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
    }

}

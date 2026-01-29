package ro.insurance.buildinginsurance.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskFactorConfigurationFacade{

    private final CityRepository cityRepository;
    private final CountyRepository countyRepository;
    private final CountryRepository countryRepository;
    private final RiskFactorConfigurationRepository riskFactorConfigurationRepository;

    public void validateCreateOrThrow(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        validateRequest(riskFactorConfigurationRequestDTO);
        checkDatesOverlappingForCreate(riskFactorConfigurationRequestDTO);
        validateRiskFactorUniquenessForCreate(riskFactorConfigurationRequestDTO);
    }

    public void validateUpdateOrThrow(Long id, RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        validateRequest(riskFactorConfigurationRequestDTO);
        checkDatesOverlappingForUpdate(id, riskFactorConfigurationRequestDTO);
        validateRiskFactorUniquenessForUpdate(id, riskFactorConfigurationRequestDTO);
    }

    private void validateRequest(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        Long referenceId = riskFactorConfigurationRequestDTO.referenceId();
        BuildingType buildingType = riskFactorConfigurationRequestDTO.buildingType();
        RiskFactorLevel level = riskFactorConfigurationRequestDTO.level();

        validateDatesOrThrow(riskFactorConfigurationRequestDTO);
        validateReferenceExclusivity(referenceId, buildingType);

        if (referenceId != null) {
            validateReferenceScope(level, referenceId);
        } else {
            validateBuildingTypeScope(level, buildingType);
        }
    }

    private void checkDatesOverlappingForCreate(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        String levelName = riskFactorConfigurationRequestDTO.level().name();
        String buildingTypeName = riskFactorConfigurationRequestDTO.buildingType() == null ? null : riskFactorConfigurationRequestDTO.buildingType().name();

        boolean overlaps = riskFactorConfigurationRepository
                .existsOverlappingIntervalForCreate(
                        levelName,
                        riskFactorConfigurationRequestDTO.referenceId(),
                        buildingTypeName,
                        riskFactorConfigurationRequestDTO.effectiveFrom(),
                        riskFactorConfigurationRequestDTO.effectiveTo()
                );

        if (overlaps) {
            log.warn("Overlapping interval on create: level={}, refId={}, buildingType={}, from={}, to={}",
                    riskFactorConfigurationRequestDTO.level(),
                    riskFactorConfigurationRequestDTO.referenceId(),
                    riskFactorConfigurationRequestDTO.buildingType(),
                    riskFactorConfigurationRequestDTO.effectiveFrom(),
                    riskFactorConfigurationRequestDTO.effectiveTo()
            );
            throw new BusinessException(ErrorCode.RISK_FACTOR_CONFIGURATION_OVERLAP);
        }
    }

    private void checkDatesOverlappingForUpdate(Long id, RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        String levelName = riskFactorConfigurationRequestDTO.level().name();
        String buildingTypeName = riskFactorConfigurationRequestDTO.buildingType() == null ? null : riskFactorConfigurationRequestDTO.buildingType().name();

        boolean overlaps = riskFactorConfigurationRepository
                .existsOverlappingIntervalForUpdate(
                        id,
                        levelName,
                        riskFactorConfigurationRequestDTO.referenceId(),
                        buildingTypeName,
                        riskFactorConfigurationRequestDTO.effectiveFrom(),
                        riskFactorConfigurationRequestDTO.effectiveTo()
                );

        if (overlaps) {
            log.warn("Overlapping interval on update: id={}, level={}, refId={}, buildingType={}, from={}, to={}",
                    id, riskFactorConfigurationRequestDTO.level(),
                    riskFactorConfigurationRequestDTO.referenceId(),
                    riskFactorConfigurationRequestDTO.buildingType(),
                    riskFactorConfigurationRequestDTO.effectiveFrom(),
                    riskFactorConfigurationRequestDTO.effectiveTo()
            );

            throw new BusinessException(ErrorCode.RISK_FACTOR_CONFIGURATION_OVERLAP);
        }
    }

    private void validateRiskFactorUniquenessForCreate(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        String levelName = riskFactorConfigurationRequestDTO.level().name();
        String buildingTypeName = riskFactorConfigurationRequestDTO.buildingType() == null ? null : riskFactorConfigurationRequestDTO.buildingType().name();

        boolean exists = riskFactorConfigurationRepository
                .existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                        levelName,
                        riskFactorConfigurationRequestDTO.referenceId(),
                        buildingTypeName,
                        riskFactorConfigurationRequestDTO.effectiveFrom()
                );
        if (exists) {
            log.warn("Duplicate risk factor config on create: level={}, refId={}, buildingType={}, from={}",
                    riskFactorConfigurationRequestDTO.level(),
                    riskFactorConfigurationRequestDTO.referenceId(),
                    riskFactorConfigurationRequestDTO.buildingType(),
                    riskFactorConfigurationRequestDTO.effectiveFrom()
            );

            throw new BusinessException(ErrorCode.DUPLICATE_RISK_FACTOR_CONFIG);
        }
    }

    private void validateRiskFactorUniquenessForUpdate(Long id, RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        String levelName = riskFactorConfigurationRequestDTO.level().name();
        String buildingTypeName = riskFactorConfigurationRequestDTO.buildingType() == null ? null : riskFactorConfigurationRequestDTO.buildingType().name();

        boolean exists = riskFactorConfigurationRepository
                .existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                        levelName,
                        riskFactorConfigurationRequestDTO.referenceId(),
                        buildingTypeName,
                        riskFactorConfigurationRequestDTO.effectiveFrom(),
                        id
                );
        if (exists) {
            log.warn("Duplicate risk factor config on update: id={}, level={}, refId={}, buildingType={}, from={}",
                    id,
                    riskFactorConfigurationRequestDTO.level(),
                    riskFactorConfigurationRequestDTO.referenceId(),
                    riskFactorConfigurationRequestDTO.buildingType(),
                    riskFactorConfigurationRequestDTO.effectiveFrom()
            );
            throw new BusinessException(ErrorCode.DUPLICATE_RISK_FACTOR_CONFIG);
        }
    }

    private void validateReferenceExclusivity(Long referenceId, BuildingType buildingType) {
        if ((referenceId == null) == (buildingType == null)) {
            log.warn("Invalid risk factor scope: referenceId={}, buildingType={}", referenceId, buildingType);
            throw new BusinessException(ErrorCode.INVALID_RISK_FACTOR_SCOPE);
        }
    }

    private void validateReferenceScope(RiskFactorLevel level, Long referenceId) {
        switch (level) {
            case CITY -> {
                if (!cityRepository.existsById(referenceId)) {
                    log.warn("City not found for risk factor scope: {}", referenceId);
                    throw new BusinessException(ErrorCode.CITY_NOT_FOUND);
                }
            }
            case COUNTY -> {
                if (!countyRepository.existsById(referenceId)) {
                    log.warn("County not found for risk factor scope: {}", referenceId);
                    throw new BusinessException(ErrorCode.COUNTY_NOT_FOUND);
                }
            }
            case COUNTRY -> {
                if (!countryRepository.existsById(referenceId)) {
                    log.warn("Country not found for risk factor scope: {}", referenceId);
                    throw new BusinessException(ErrorCode.COUNTRY_NOT_FOUND);
                }
            }
            case BUILDING_TYPE -> {
                log.warn("Invalid risk factor scope: level=BUILDING_TYPE requires buildingType, not referenceId={}", referenceId);
                throw new BusinessException(ErrorCode.INVALID_RISK_FACTOR_SCOPE);
            }
        }
    }

    private void validateBuildingTypeScope(RiskFactorLevel level, BuildingType buildingType) {
        if (level != RiskFactorLevel.BUILDING_TYPE) {
            log.warn("Invalid risk factor scope: level={}, buildingType={}", level, buildingType);
            throw new BusinessException(ErrorCode.INVALID_RISK_FACTOR_SCOPE);
        }
    }

    private void validateDatesOrThrow(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        LocalDate effectiveFrom = riskFactorConfigurationRequestDTO.effectiveFrom();
        LocalDate effectiveTo = riskFactorConfigurationRequestDTO.effectiveTo();

        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            log.warn("Invalid dates, effectiveFrom={}, effectiveTo={}", effectiveFrom, effectiveTo);
            throw new BusinessException(ErrorCode.INVALID_DATES);
        }
    }


}

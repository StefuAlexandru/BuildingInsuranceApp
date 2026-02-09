package ro.insurance.buildinginsurance.application.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;
import static ro.insurance.buildinginsurance.util.RiskFactorConfigurationTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class RiskFactorConfigurationFacadeTest {

    @Mock private CityRepository cityRepository;
    @Mock private CountyRepository countyRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private RiskFactorConfigurationRepository riskFactorConfigurationRepository;

    @InjectMocks
    private RiskFactorConfigurationFacade facade;

    @Test
    void validateCreateOrThrow_shouldPass_whenCityScopeValid_noOverlap_noDuplicate() {
        // Arrange
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(), // buildingTypeName must be null for CITY/COUNTY/COUNTRY
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(false);

        when(riskFactorConfigurationRepository.existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom())
        )).thenReturn(false);

        // Act + Assert
        assertDoesNotThrow(() -> facade.validateCreateOrThrow(request));

        InOrder inOrder = inOrder(cityRepository, riskFactorConfigurationRepository);
        inOrder.verify(cityRepository).existsById(request.referenceId());
        inOrder.verify(riskFactorConfigurationRepository).existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        inOrder.verify(riskFactorConfigurationRepository).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom())
        );

        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenReferenceIdAndBuildingTypeAreBothNull() {
        // Arrange
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestInvalidBothNull();

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        // Assert
        assertHasError(ex,ErrorCode.INVALID_RISK_FACTOR_SCOPE);

        verifyNoInteractions(cityRepository, countyRepository, countryRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenReferenceIdAndBuildingTypeAreBothSet() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestInvalidBothSet();

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.INVALID_RISK_FACTOR_SCOPE);

        verifyNoInteractions(cityRepository, countyRepository, countryRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenDatesInvalid() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestInvalidDates();

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.INVALID_DATES);

        verifyNoInteractions(cityRepository, countyRepository, countryRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenLevelIsBuildingTypeButReferenceIdProvided() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestInvalidBuildingTypeLevelWithReferenceId();

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.INVALID_RISK_FACTOR_SCOPE);

        verifyNoInteractions(cityRepository, countyRepository, countryRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenLevelIsCityButBuildingTypeProvided() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestInvalidCityLevelWithBuildingType();

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.INVALID_RISK_FACTOR_SCOPE);

        verifyNoInteractions(cityRepository, countyRepository, countryRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenCityNotFound() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.CITY_NOT_FOUND);

        verify(cityRepository).existsById(request.referenceId());
        verifyNoInteractions(countyRepository, countryRepository, riskFactorConfigurationRepository);
        verifyNoMoreInteractions(cityRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenCountyNotFound() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCounty();

        when(countyRepository.existsById(request.referenceId())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.COUNTY_NOT_FOUND);

        verify(countyRepository).existsById(request.referenceId());
        verifyNoInteractions(cityRepository, countryRepository, riskFactorConfigurationRepository);
        verifyNoMoreInteractions(countyRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenCountryNotFound() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCountry();

        when(countryRepository.existsById(request.referenceId())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.COUNTRY_NOT_FOUND);

        verify(countryRepository).existsById(request.referenceId());
        verifyNoInteractions(cityRepository, countyRepository, riskFactorConfigurationRepository);
        verifyNoMoreInteractions(countryRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenOverlapExists_cityScope() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.RISK_FACTOR_CONFIGURATION_OVERLAP);

        verify(cityRepository).existsById(request.referenceId());
        verify(riskFactorConfigurationRepository).existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        verify(riskFactorConfigurationRepository, never()).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(any(), any(), any(), any());

        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldThrow_whenDuplicateExists_cityScope() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(false);

        when(riskFactorConfigurationRepository.existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom())
        )).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateCreateOrThrow(request));

        assertHasError(ex,ErrorCode.DUPLICATE_RISK_FACTOR_CONFIG);

        InOrder inOrder = inOrder(cityRepository, riskFactorConfigurationRepository);
        inOrder.verify(cityRepository).existsById(request.referenceId());
        inOrder.verify(riskFactorConfigurationRepository).existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        inOrder.verify(riskFactorConfigurationRepository).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom())
        );

        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateCreateOrThrow_shouldPass_whenBuildingTypeScopeValid_noOverlap_noDuplicate() {
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestBuildingType();

        when(riskFactorConfigurationRepository.existsOverlappingIntervalForCreate(
                eq(request.level().name()),
                isNull(), // referenceId null for BUILDING_TYPE
                eq(request.buildingType().name()),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(false);

        when(riskFactorConfigurationRepository.existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()),
                isNull(),
                eq(request.buildingType().name()),
                eq(request.effectiveFrom())
        )).thenReturn(false);

        assertDoesNotThrow(() -> facade.validateCreateOrThrow(request));

        verifyNoInteractions(cityRepository, countyRepository, countryRepository);
        verify(riskFactorConfigurationRepository).existsOverlappingIntervalForCreate(
                eq(request.level().name()), isNull(), eq(request.buildingType().name()), eq(request.effectiveFrom()), eq(request.effectiveTo())
        );
        verify(riskFactorConfigurationRepository).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                eq(request.level().name()), isNull(), eq(request.buildingType().name()), eq(request.effectiveFrom())
        );
        verifyNoMoreInteractions(riskFactorConfigurationRepository);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void validateUpdateOrThrow_shouldThrow_whenOverlapExists() {
        Long id = 1L;
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateUpdateOrThrow(id, request));

        assertHasError(ex,ErrorCode.RISK_FACTOR_CONFIGURATION_OVERLAP);

        verify(cityRepository).existsById(request.referenceId());
        verify(riskFactorConfigurationRepository).existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        verify(riskFactorConfigurationRepository, never()).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(any(), any(), any(), any(), anyLong());

        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateUpdateOrThrow_shouldThrow_whenDuplicateExists() {
        Long id = 1L;
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(false);

        when(riskFactorConfigurationRepository.existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(id)
        )).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> facade.validateUpdateOrThrow(id, request));

        assertHasError(ex,ErrorCode.DUPLICATE_RISK_FACTOR_CONFIG);

        InOrder inOrder = inOrder(cityRepository, riskFactorConfigurationRepository);
        inOrder.verify(cityRepository).existsById(request.referenceId());
        inOrder.verify(riskFactorConfigurationRepository).existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        inOrder.verify(riskFactorConfigurationRepository).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(id)
        );

        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }

    @Test
    void validateUpdateOrThrow_shouldPass_whenValid_cityScope_noOverlap_noDuplicate() {
        Long id = 1L;
        RiskFactorConfigurationRequestDTO request = aRiskFactorConfigurationRequestCity();

        when(cityRepository.existsById(request.referenceId())).thenReturn(true);
        when(riskFactorConfigurationRepository.existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        )).thenReturn(false);

        when(riskFactorConfigurationRepository.existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(id)
        )).thenReturn(false);

        assertDoesNotThrow(() -> facade.validateUpdateOrThrow(id, request));

        verify(cityRepository).existsById(request.referenceId());
        verify(riskFactorConfigurationRepository).existsOverlappingIntervalForUpdate(
                eq(id),
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(request.effectiveTo())
        );
        verify(riskFactorConfigurationRepository).existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                eq(request.level().name()),
                eq(request.referenceId()),
                isNull(),
                eq(request.effectiveFrom()),
                eq(id)
        );
        verifyNoInteractions(countyRepository, countryRepository);
        verifyNoMoreInteractions(cityRepository, riskFactorConfigurationRepository);
    }


}

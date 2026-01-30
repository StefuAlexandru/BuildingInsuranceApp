package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.PolicyCalculationService;
import ro.insurance.buildinginsurance.domain.entity.Building;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BuildingRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.util.BuildingTestDataFactory;
import ro.insurance.buildinginsurance.util.CurrencyTestDataFactory;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.FeeConfigurationTestDataFactory.aFee;
import static ro.insurance.buildinginsurance.util.PolicyCalculationTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.RiskFactorConfigurationTestDataFactory.aRisk;

@ExtendWith(MockitoExtension.class)
class PolicyCalculationServiceTest {
    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private FeeConfigurationRepository feeConfigurationRepository;
    @Mock
    private RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private PolicyCalculationService policyCalculationService;

    @Test
    void calculatePremium_noFeesNoRisks_shouldReturnBasePremium() {
        // Arrange
        PolicyCalculationRequestDTO request = aPolicyCalculationRequest();
        Building building = aBuildingWithId(1L);
        Currency currency = aCurrencyWithId(1L);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(feeConfigurationRepository.findAllActiveOn(request.now())).thenReturn(List.of());
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, request.now())).thenReturn(List.of());

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(request);

        // Assert
        assertAll(
                () -> assertEquals(new BigDecimal("100.00"), res.finalPremium()),
                () -> assertEquals(new BigDecimal("0"), res.totalPercent()),
                () -> assertNotNull(res.breakdown()),
                () -> assertTrue(res.breakdown().isEmpty())
        );

        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(request.now());
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, request.now());
        verifyNoMoreInteractions(buildingRepository, currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_multipleAdjustments_shouldApplyFormula() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 30);
        PolicyCalculationRequestDTO req = aPolicyCalculationRequest();

        Building building = BuildingTestDataFactory.aBuildingWithId(1L);
        Currency currency = CurrencyTestDataFactory.aCurrencyWithId(1L);

        FeeConfiguration fee1 = aFee(FeeType.ADMIN_FEE, new BigDecimal("0.10"));
        FeeConfiguration fee2 = aFee(FeeType.RISK_ADJUSTMENT, new BigDecimal("0.05"));

        RiskFactorConfiguration r1 = aRisk(RiskFactorLevel.CITY, 123L, null, new BigDecimal("0.20"));
        RiskFactorConfiguration r2 = aRisk(RiskFactorLevel.BUILDING_TYPE, null, BuildingType.RESIDENTIAL, new BigDecimal("-0.05"));

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(feeConfigurationRepository.findAllActiveOn(date)).thenReturn(List.of(fee1, fee2));
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, date)).thenReturn(List.of(r1, r2));

        // totalPercent = 0.10 + 0.05 + 0.20 - 0.05 = 0.30
        // final = 100 * (1 + 0.30) = 130.00
        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(req);

        // Assert
        assertAll(
                () -> assertEquals(new BigDecimal("130.00"), res.finalPremium()),
                () -> assertEquals(new BigDecimal("0.30"), res.totalPercent()),
                () -> assertEquals(4, res.breakdown().size())
        );

        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(date);
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, date);
        verifyNoMoreInteractions(buildingRepository, currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_nowNull_shouldUseToday_andCallReposWithToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        PolicyCalculationRequestDTO request = aPolicyCalculationRequestWithDate(null);

        Building building = aBuildingWithId(1L);
        Currency currency = aCurrencyWithId(1L);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(feeConfigurationRepository.findAllActiveOn(today)).thenReturn(List.of());
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, today)).thenReturn(List.of());

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(request);

        // Assert
        assertEquals(new BigDecimal("100.00"), res.finalPremium());

        verify(feeConfigurationRepository).findAllActiveOn(today);
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, today);
        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(1L);
        verifyNoMoreInteractions(buildingRepository, currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_buildingNotFound_shouldThrowBusinessException() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 30);
        PolicyCalculationRequestDTO request = aPolicyCalculationRequestWithBuildingId(333L);

        when(buildingRepository.findById(333L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> policyCalculationService.calculatePremium(request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findById(333L);
        verifyNoMoreInteractions(buildingRepository);
        verifyNoInteractions(currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_currencyNotFound_shouldThrowBusinessException() {
        // Arrange
        PolicyCalculationRequestDTO request = aPolicyCalculationRequestWithCurrencyId(333L);

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(aBuildingWithId(1L)));
        when(currencyRepository.findById(333L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> policyCalculationService.calculatePremium(request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CURRENCY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(333L);
        verifyNoMoreInteractions(buildingRepository, currencyRepository);
        verifyNoInteractions(feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_currencyInactive_shouldThrowBusinessException() {
        // Arrange
        PolicyCalculationRequestDTO request = aPolicyCalculationRequest();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(aBuildingWithId(1L)));

        Currency inactive = aCurrencyWithId(1L);
        inactive.setActive(false);
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(inactive));

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> policyCalculationService.calculatePremium(request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CURRENCY_IS_NOT_ACTIVE.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(1L);
        verifyNoMoreInteractions(buildingRepository, currencyRepository);
        verifyNoInteractions(feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_rounding_shouldBeHalfUpAt2Decimals() {
        // Arrange
        PolicyCalculationRequestDTO request = aPolicyCalculationRequest();

        // totalPercent = 0.005 => final = 100 * 1.005 = 100.5 -> "100.50"
        FeeConfiguration fee = aFee(FeeType.ADMIN_FEE, new BigDecimal("0.005"));

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(aBuildingWithId(1L)));
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(aCurrencyWithId(1L)));
        when(feeConfigurationRepository.findAllActiveOn(request.now())).thenReturn(List.of(fee));
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, request.now())).thenReturn(List.of());

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(request);

        // Assert
        assertEquals(new BigDecimal("100.50"), res.finalPremium());

        verify(buildingRepository).findById(1L);
        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(request.now());
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, request.now());
        verifyNoMoreInteractions(buildingRepository, currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }
}
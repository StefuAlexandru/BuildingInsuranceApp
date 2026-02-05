package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.PolicyCalculationService;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
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
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;
import static ro.insurance.buildinginsurance.util.FeeConfigurationTestDataFactory.aFee;
import static ro.insurance.buildinginsurance.util.PolicyCalculationTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.RiskFactorConfigurationTestDataFactory.aRisk;

@ExtendWith(MockitoExtension.class)
class PolicyCalculationServiceTest {
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
        Currency currency = aCurrencyWithId(1L);

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

        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(request.now());
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, request.now());
        verifyNoMoreInteractions(currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_multipleAdjustments_shouldApplyFormula() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 1, 30);
        PolicyCalculationRequestDTO req = aPolicyCalculationRequest();

        Currency currency = CurrencyTestDataFactory.aCurrencyWithId(1L);

        FeeConfiguration fee1 = aFee(FeeType.ADMIN_FEE, new BigDecimal("10"));
        FeeConfiguration fee2 = aFee(FeeType.RISK_ADJUSTMENT, new BigDecimal("5"));

        RiskFactorConfiguration r1 = aRisk(RiskFactorLevel.CITY, 123L, null, new BigDecimal("20"));
        RiskFactorConfiguration r2 = aRisk(RiskFactorLevel.BUILDING_TYPE, null, BuildingType.RESIDENTIAL, new BigDecimal("-5"));

        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(feeConfigurationRepository.findAllActiveOn(date)).thenReturn(List.of(fee1, fee2));
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, date)).thenReturn(List.of(r1, r2));

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(req);

        // Assert
        assertAll(
                () -> assertEquals(new BigDecimal("130.00"), res.finalPremium()),
                () -> assertEquals(new BigDecimal("30"), res.totalPercent()),
                () -> assertEquals(4, res.breakdown().size())
        );

        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(date);
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, date);
        verifyNoMoreInteractions(currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_nowNull_shouldUseToday_andCallReposWithToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        PolicyCalculationRequestDTO request = aPolicyCalculationRequestWithDate(null);

        Currency currency = aCurrencyWithId(1L);

        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(feeConfigurationRepository.findAllActiveOn(today)).thenReturn(List.of());
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, today)).thenReturn(List.of());

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(request);

        // Assert
        assertEquals(new BigDecimal("100.00"), res.finalPremium());

        verify(feeConfigurationRepository).findAllActiveOn(today);
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, today);
        verify(currencyRepository).findById(1L);
        verifyNoMoreInteractions(currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }


    @Test
    void calculatePremium_rounding_shouldBeHalfUpAt2Decimals() {
        // Arrange
        PolicyCalculationRequestDTO request = aPolicyCalculationRequest();

        FeeConfiguration fee = aFee(FeeType.ADMIN_FEE, new BigDecimal("0.5"));

        when(currencyRepository.findById(1L)).thenReturn(Optional.of(aCurrencyWithId(1L)));
        when(feeConfigurationRepository.findAllActiveOn(request.now())).thenReturn(List.of(fee));
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, request.now())).thenReturn(List.of());

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(request);

        // Assert
        assertEquals(new BigDecimal("100.50"), res.finalPremium());

        verify(currencyRepository).findById(1L);
        verify(feeConfigurationRepository).findAllActiveOn(request.now());
        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(1L, request.now());
        verifyNoMoreInteractions(currencyRepository, feeConfigurationRepository, riskFactorConfigurationRepository);
    }

    @Test
    void calculatePremium_shouldConvertFromRON_whenCurrencyIsNotRON() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 4);

        when(feeConfigurationRepository.findAllActiveOn(date)).thenReturn(List.of());
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, date)).thenReturn(List.of());

        Currency eur = new Currency();
        eur.setId(2L);
        eur.setCode("EUR");
        eur.setExchangeRateToBase(new BigDecimal("5")); // 1 EUR = 5 RON (rate to base)
        when(currencyRepository.findById(2L)).thenReturn(Optional.of(eur));

        PolicyCalculationRequestDTO req = new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L, 2L, date);

        // Act
        PolicyCalculationResponseDTO res = policyCalculationService.calculatePremium(req);

        // Assert
        // 100 RON / 5 = 20 EUR
        assertEquals(0, new BigDecimal("20.00").compareTo(res.finalPremium()));
        assertEquals(0, new BigDecimal("0").compareTo(res.totalPercent()));
        assertTrue(res.breakdown().isEmpty());
    }

    @Test
    void calculatePremium_shouldThrow_whenCurrencyNotFound() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 4);

        when(feeConfigurationRepository.findAllActiveOn(date)).thenReturn(List.of());
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, date)).thenReturn(List.of());
        when(currencyRepository.findById(999L)).thenReturn(Optional.empty());

        PolicyCalculationRequestDTO req = new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L, 999L, date);// Act

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyCalculationService.calculatePremium(req));
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CURRENCY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );
    }

    @Test
    void calculatePremium_shouldThrow_whenFinalPremiumNonPositive() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 2, 4);

        FeeConfiguration fee = new FeeConfiguration();
        fee.setFeeType(FeeType.ADMIN_FEE);
        fee.setPercentage(new BigDecimal("-200"));

        when(feeConfigurationRepository.findAllActiveOn(date)).thenReturn(List.of(fee));
        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(1L, date)).thenReturn(List.of());

        PolicyCalculationRequestDTO req = new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L, 1L, date);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> policyCalculationService.calculatePremium(req));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_FINAL_PREMIUM.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );
    }
}
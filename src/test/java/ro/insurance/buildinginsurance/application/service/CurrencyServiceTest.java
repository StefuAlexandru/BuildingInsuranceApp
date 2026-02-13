package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ro.insurance.buildinginsurance.application.mapper.CurrencyMapper;
import ro.insurance.buildinginsurance.application.service.impl.CurrencyService;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currency_dto.CurrencyResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.data_factory.CurrencyTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyService currencyService;

    // --------------------- CREATE ---------------------

    @Test
    void registerCurrency_shouldCreateAndReturnResponse() {
        // Arrange
        CurrencyRequestDTO request = aCurrencyRequest();
        Currency entity = aCurrencyEntity(null, "RON", "Romanian Leu", request.exchangeRateToBase(), true);
        Currency saved = aCurrencyEntity(1L, "RON", "Romanian Leu", request.exchangeRateToBase(), true);
        CurrencyResponseDTO response = aCurrencyResponse();

        when(currencyRepository.existsByCodeIgnoreCase("RON")).thenReturn(false);
        when(currencyMapper.toEntity(request)).thenReturn(entity);
        when(currencyRepository.save(entity)).thenReturn(saved);
        when(currencyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.registerCurrency(request);

        // Assert
        assertEquals(response, result);

        verify(currencyRepository).existsByCodeIgnoreCase("RON");
        verify(currencyMapper).toEntity(request);
        verify(currencyRepository).save(entity);
        verify(currencyMapper).toResponse(saved);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void registerCurrency_shouldTrimAndUppercaseCodeBeforeUniquenessCheck() {
        // Arrange
        CurrencyRequestDTO request = aCurrencyRequestWithCode("   rOn  ");
        Currency entity = aCurrencyEntity(null, "RON", "Romanian Leu", request.exchangeRateToBase(), true);
        Currency saved = aCurrencyEntity(1L, "RON", "Romanian Leu", request.exchangeRateToBase(), true);
        CurrencyResponseDTO response = aCurrencyResponse();

        when(currencyRepository.existsByCodeIgnoreCase("RON")).thenReturn(false);
        when(currencyMapper.toEntity(request)).thenReturn(entity);
        when(currencyRepository.save(entity)).thenReturn(saved);
        when(currencyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.registerCurrency(request);

        // Assert
        assertEquals(response, result);

        verify(currencyRepository).existsByCodeIgnoreCase("RON");
        verify(currencyMapper).toEntity(request);
        verify(currencyRepository).save(entity);
        verify(currencyMapper).toResponse(saved);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void registerCurrency_shouldThrowWhenCodeAlreadyExists() {
        // Arrange
        CurrencyRequestDTO request = aCurrencyRequest();

        when(currencyRepository.existsByCodeIgnoreCase("RON")).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> currencyService.registerCurrency(request));

        // Assert
        assertHasError(ex,ErrorCode.CURRENCY_CODE_ALREADY_EXISTS);

        verify(currencyRepository).existsByCodeIgnoreCase("RON");
        verifyNoMoreInteractions(currencyRepository);
        verifyNoInteractions(currencyMapper, policyRepository);
    }

    // --------------------- GET ---------------------

    @Test
    void getAllCurrencies_shouldReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by("code").ascending());

        Currency c1 = aCurrencyWithIdCodeAndActive(1L, "RON", true);
        Currency c2 = aCurrencyWithIdCodeAndActive(2L, "EUR", false);

        Page<Currency> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        CurrencyResponseDTO r1 = aCurrencyResponse(1L, "RON", "Romanian Leu", c1.getExchangeRateToBase(), true);
        CurrencyResponseDTO r2 = aCurrencyResponse(2L, "EUR", "Romanian Leu", c2.getExchangeRateToBase(), false);

        when(currencyRepository.findAll(pageable)).thenReturn(page);
        when(currencyMapper.toResponse(c1)).thenReturn(r1);
        when(currencyMapper.toResponse(c2)).thenReturn(r2);

        // Act
        Page<CurrencyResponseDTO> result = currencyService.getAllCurrencies(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(r1, result.getContent().get(0));
        assertEquals(r2, result.getContent().get(1));

        verify(currencyRepository).findAll(pageable);
        verify(currencyMapper).toResponse(c1);
        verify(currencyMapper).toResponse(c2);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    // --------------------- ACTIVATE ---------------------

    @Test
    void activate_shouldSetActiveTrueAndSave() {
        // Arrange
        Currency existing = aCurrencyWithIdCodeAndActive(10L, "EUR", false);
        Currency saved = aCurrencyWithIdCodeAndActive(10L, "EUR", true);
        CurrencyResponseDTO response = aCurrencyResponse(10L, "EUR", existing.getName(), existing.getExchangeRateToBase(), true);

        when(currencyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(currencyRepository.save(existing)).thenReturn(saved);
        when(currencyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.activate(10L);

        // Assert
        assertEquals(response, result);
        assertTrue(existing.isActive());

        verify(currencyRepository).findById(10L);
        verify(currencyRepository).save(existing);
        verify(currencyMapper).toResponse(saved);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void activate_whenAlreadyActive_shouldReturnWithoutSaving() {
        // Arrange
        Currency existing = aCurrencyWithIdCodeAndActive(10L, "EUR", true);
        CurrencyResponseDTO response = aCurrencyResponse(10L, "EUR", existing.getName(), existing.getExchangeRateToBase(), true);

        when(currencyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(currencyMapper.toResponse(existing)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.activate(10L);

        // Assert
        assertEquals(response, result);

        verify(currencyRepository).findById(10L);
        verify(currencyMapper).toResponse(existing);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void activate_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        when(currencyRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> currencyService.activate(99L));

        // Assert
        assertHasError(ex,ErrorCode.CURRENCY_NOT_FOUND);

        verify(currencyRepository).findById(99L);
        verifyNoMoreInteractions(currencyRepository);
        verifyNoInteractions(currencyMapper, policyRepository);
    }

    // --------------------- DEACTIVATE ---------------------

    @Test
    void deactivate_whenAlreadyInactive_shouldReturnWithoutPolicyCheckOrSave() {
        // Arrange
        Currency existing = aCurrencyWithIdCodeAndActive(10L, "EUR", false);
        CurrencyResponseDTO response = aCurrencyResponse(10L, "EUR", existing.getName(), existing.getExchangeRateToBase(), false);

        when(currencyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(currencyMapper.toResponse(existing)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.deactivate(10L);

        // Assert
        assertEquals(response, result);

        verify(currencyRepository).findById(10L);
        verify(currencyMapper).toResponse(existing);
        verifyNoMoreInteractions(currencyRepository, currencyMapper);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void deactivate_shouldThrowWhenCurrencyUsedByActivePolicies() {
        // Arrange
        Currency existing = aCurrencyWithIdCodeAndActive(10L, "EUR", true);

        when(currencyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(policyRepository.existsActivePolicyByCurrencyCode("EUR")).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> currencyService.deactivate(10L));

        // Assert
        assertHasError(ex,ErrorCode.CURRENCY_IN_USE_BY_ACTIVE_POLICIES);

        verify(currencyRepository).findById(10L);
        verify(policyRepository).existsActivePolicyByCurrencyCode("EUR");
        verifyNoMoreInteractions(currencyRepository, policyRepository);
        verifyNoInteractions(currencyMapper);
    }

    @Test
    void deactivate_shouldSetActiveFalseAndSave_whenNoActivePolicies() {
        // Arrange
        Currency existing = aCurrencyWithIdCodeAndActive(10L, "EUR", true);
        Currency saved = aCurrencyWithIdCodeAndActive(10L, "EUR", false);
        CurrencyResponseDTO response = aCurrencyResponse(10L, "EUR", existing.getName(), existing.getExchangeRateToBase(), false);

        when(currencyRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(policyRepository.existsActivePolicyByCurrencyCode("EUR")).thenReturn(false);
        when(currencyRepository.save(existing)).thenReturn(saved);
        when(currencyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CurrencyResponseDTO result = currencyService.deactivate(10L);

        // Assert
        assertEquals(response, result);
        assertFalse(existing.isActive());

        verify(currencyRepository).findById(10L);
        verify(policyRepository).existsActivePolicyByCurrencyCode("EUR");
        verify(currencyRepository).save(existing);
        verify(currencyMapper).toResponse(saved);
        verifyNoMoreInteractions(currencyRepository, policyRepository, currencyMapper);
    }

    @Test
    void deactivate_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        when(currencyRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> currencyService.deactivate(99L));

        // Assert
        assertHasError(ex,ErrorCode.CURRENCY_NOT_FOUND);

        verify(currencyRepository).findById(99L);
        verifyNoMoreInteractions(currencyRepository);
        verifyNoInteractions(currencyMapper, policyRepository);
    }
}

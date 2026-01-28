package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ro.insurance.buildinginsurance.application.mapper.FeeConfigurationMapper;
import ro.insurance.buildinginsurance.application.service.impl.FeeConfigurationService;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.FeeConfigurationTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class FeeConfigurationServiceTest {

    @Mock
    private FeeConfigurationRepository feeConfigurationRepository;
    @Mock
    private FeeConfigurationMapper feeConfigurationMapper;

    @InjectMocks
    private FeeConfigurationService feeConfigurationService;

    // ---------------------  CREATE  ---------------------

    @Test
    void registerFee_shouldCreateAndReturnResponse() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        String name = request.name().trim();

        FeeConfiguration entity = aFeeWithoutId();
        FeeConfiguration saved = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo())).thenReturn(false);
        when(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom())).thenReturn(false);

        when(feeConfigurationMapper.toEntity(request)).thenReturn(entity);
        when(feeConfigurationRepository.save(entity)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        FeeConfigurationResponseDTO result = feeConfigurationService.registerFee(request);

        assertEquals(response, result);

        verify(feeConfigurationRepository).existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo());
        verify(feeConfigurationRepository).existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom());
        verify(feeConfigurationMapper).toEntity(request);
        verify(feeConfigurationRepository).save(entity);
        verify(feeConfigurationMapper).toResponse(saved);
    }

    @Test
    void registerFee_shouldTrimNameBeforeChecks() {
        FeeConfigurationRequestDTO request = aFeeRequestWithName("   Standard admin fee   ");
        String name = "Standard admin fee";

        FeeConfiguration entity = aFeeWithoutId();
        FeeConfiguration saved = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo())).thenReturn(false);
        when(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom())).thenReturn(false);

        when(feeConfigurationMapper.toEntity(request)).thenReturn(entity);
        when(feeConfigurationRepository.save(entity)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        FeeConfigurationResponseDTO result = feeConfigurationService.registerFee(request);

        assertEquals(response, result);

        verify(feeConfigurationRepository).existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo());
        verify(feeConfigurationRepository).existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom());
    }

    @Test
    void registerFee_shouldThrowWhenInvalidDates() {
        FeeConfigurationRequestDTO request = aFeeRequestWithDates(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.registerFee(request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_DATES.name().equals(e.getCode())));
        verifyNoInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void registerFee_shouldThrowWhenExactKeyAlreadyExists() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        String name = request.name().trim();

        when(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.registerFee(request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS.name().equals(e.getCode())));

        verify(feeConfigurationRepository).existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(name, request.feeType().name(), request.effectiveFrom());
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void registerFee_shouldThrowWhenOverlappingIntervalExists() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        String name = request.name().trim();

        when(feeConfigurationRepository.existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.registerFee(request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_OVERLAP.name().equals(e.getCode())));

        verify(feeConfigurationRepository).existsOverlappingIntervalForCreate(request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo());
        verifyNoInteractions(feeConfigurationMapper);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getFeeById_shouldReturnResponse() {
        FeeConfiguration entity = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(feeConfigurationMapper.toResponse(entity)).thenReturn(response);

        FeeConfigurationResponseDTO result = feeConfigurationService.getFeeById(1L);

        assertEquals(response, result);
        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationMapper).toResponse(entity);
    }

    @Test
    void getFeeById_whenNotFound_shouldThrowBusinessException() {
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.getFeeById(1L));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_NOT_FOUND.name().equals(e.getCode())));
        verify(feeConfigurationRepository).findById(1L);
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void getAllFeesActiveOn_shouldReturnPagedResponse() {
        LocalDate date = LocalDate.of(2026, 1, 28);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        FeeConfiguration fee1 = aFeeWithId(1L);
        FeeConfiguration fee2 = aFeeWithId(2L);

        Page<FeeConfiguration> page = new PageImpl<>(List.of(fee1, fee2), pageable, 2);

        FeeConfigurationResponseDTO resp1 = aFeeResponse();
        FeeConfigurationResponseDTO resp2 = new FeeConfigurationResponseDTO(
                2L, "Standard admin fee 2", resp1.feeType(), resp1.percentage(),
                resp1.effectiveFrom(), resp1.effectiveTo(), true
        );

        when(feeConfigurationRepository.getAllFeesActiveOn(date, pageable)).thenReturn(page);
        when(feeConfigurationMapper.toResponse(fee1)).thenReturn(resp1);
        when(feeConfigurationMapper.toResponse(fee2)).thenReturn(resp2);

        Page<FeeConfigurationResponseDTO> result = feeConfigurationService.getAllFeesActiveOn(date, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(resp1, result.getContent().get(0));
        assertEquals(resp2, result.getContent().get(1));

        verify(feeConfigurationRepository).getAllFeesActiveOn(date, pageable);
        verify(feeConfigurationMapper).toResponse(fee1);
        verify(feeConfigurationMapper).toResponse(fee2);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateFee_shouldUpdateAndReturnResponse() {
        FeeConfigurationRequestDTO request = aFeeRequestWithName("Updated admin fee");
        String name = request.name().trim();

        FeeConfiguration existing = aFeeWithId(1L);
        FeeConfiguration saved = aFeeWithId(1L);

        FeeConfigurationResponseDTO response = new FeeConfigurationResponseDTO(
                1L, "Updated admin fee", request.feeType(), request.percentage(),
                request.effectiveFrom(), request.effectiveTo(), true
        );

        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(feeConfigurationRepository.existsOverlappingIntervalForUpdate(1L, request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo())).thenReturn(false);
        when(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(name, request.feeType().name(), request.effectiveFrom(), 1L)).thenReturn(false);

        when(feeConfigurationRepository.save(existing)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        FeeConfigurationResponseDTO result = feeConfigurationService.updateFee(1L, request);

        assertEquals(response, result);

        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationRepository).existsOverlappingIntervalForUpdate(1L, request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo());
        verify(feeConfigurationRepository).existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(name, request.feeType().name(), request.effectiveFrom(), 1L);
        verify(feeConfigurationMapper).updateFromDTO(request, existing);
        verify(feeConfigurationRepository).save(existing);
        verify(feeConfigurationMapper).toResponse(saved);
    }

    @Test
    void updateFee_whenNotFound_shouldThrowBusinessException() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_NOT_FOUND.name().equals(e.getCode())));
        verify(feeConfigurationRepository).findById(1L);
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void updateFee_shouldThrowWhenInvalidDates() {
        FeeConfigurationRequestDTO request = aFeeRequestWithDates(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );

        FeeConfiguration existing = aFeeWithId(1L);
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_DATES.name().equals(e.getCode())));

        verify(feeConfigurationRepository).findById(1L);
        verifyNoMoreInteractions(feeConfigurationRepository);
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void updateFee_shouldThrowWhenOverlappingIntervalExists() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        String name = request.name().trim();

        FeeConfiguration existing = aFeeWithId(1L);
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(feeConfigurationRepository.existsOverlappingIntervalForUpdate(1L, request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_OVERLAP.name().equals(e.getCode())));

        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationRepository).existsOverlappingIntervalForUpdate(1L, request.feeType().name(), name, request.effectiveFrom(), request.effectiveTo());
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void updateFee_shouldThrowWhenExactKeyExistsExcludingId() {
        FeeConfigurationRequestDTO request = aFeeRequest();
        String name = request.name().trim();

        FeeConfiguration existing = aFeeWithId(1L);
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(name, request.feeType().name(), request.effectiveFrom(), 1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));

        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS.name().equals(e.getCode())));

        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationRepository).existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(name, request.feeType().name(), request.effectiveFrom(), 1L);
        verifyNoInteractions(feeConfigurationMapper);
    }
}

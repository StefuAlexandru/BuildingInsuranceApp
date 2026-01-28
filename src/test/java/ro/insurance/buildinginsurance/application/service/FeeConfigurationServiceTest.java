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
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequest();
        FeeConfiguration entity = aFeeWithoutId();
        FeeConfiguration saved = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.existsByNameAndFeeTypeIgnoreCase(request.name().trim(), request.feeType().name())).thenReturn(false);
        when(feeConfigurationMapper.toEntity(request)).thenReturn(entity);
        when(feeConfigurationRepository.save(entity)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        // Act
        FeeConfigurationResponseDTO result = feeConfigurationService.registerFee(request);

        // Assert
        assertEquals(response, result);

        verify(feeConfigurationRepository).existsByNameAndFeeTypeIgnoreCase(request.name().trim(), request.feeType().name());
        verify(feeConfigurationMapper).toEntity(request);
        verify(feeConfigurationRepository).save(entity);
        verify(feeConfigurationMapper).toResponse(saved);
        verifyNoMoreInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void registerFee_shouldTrimNameBeforeUniquenessCheck() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequestWithName("   Standard admin fee   ");
        FeeConfiguration entity = aFeeWithoutId();
        FeeConfiguration saved = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.existsByNameAndFeeTypeIgnoreCase("Standard admin fee", request.feeType().name())).thenReturn(false);
        when(feeConfigurationMapper.toEntity(request)).thenReturn(entity);
        when(feeConfigurationRepository.save(entity)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        // Act
        FeeConfigurationResponseDTO result = feeConfigurationService.registerFee(request);

        // Assert
        assertEquals(response, result);

        verify(feeConfigurationRepository).existsByNameAndFeeTypeIgnoreCase("Standard admin fee", request.feeType().name());
        verify(feeConfigurationMapper).toEntity(request);
        verify(feeConfigurationRepository).save(entity);
        verify(feeConfigurationMapper).toResponse(saved);
        verifyNoMoreInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void registerFee_shouldThrowWhenInvalidDates() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequestWithDates(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.registerFee(request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_DATES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verifyNoInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void registerFee_shouldThrowWhenFeeAlreadyExists() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequest();

        when(feeConfigurationRepository.existsByNameAndFeeTypeIgnoreCase(request.name().trim(), request.feeType().name())).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.registerFee(request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(feeConfigurationRepository).existsByNameAndFeeTypeIgnoreCase(request.name().trim(), request.feeType().name());
        verifyNoMoreInteractions(feeConfigurationRepository);
        verifyNoInteractions(feeConfigurationMapper);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getFeeById_shouldReturnResponse() {
        // Arrange
        FeeConfiguration entity = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = aFeeResponse();

        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(feeConfigurationMapper.toResponse(entity)).thenReturn(response);

        // Act
        FeeConfigurationResponseDTO result = feeConfigurationService.getFeeById(1L);

        // Assert
        assertEquals(response, result);

        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationMapper).toResponse(entity);
        verifyNoMoreInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void getFeeById_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.getFeeById(1L));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(feeConfigurationRepository).findById(1L);
        verifyNoMoreInteractions(feeConfigurationRepository);
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void getAllFeesActiveOn_shouldReturnPagedResponse() {
        // Arrange
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

        // Act
        Page<FeeConfigurationResponseDTO> result = feeConfigurationService.getAllFeesActiveOn(date, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(resp1, result.getContent().get(0));
        assertEquals(resp2, result.getContent().get(1));

        verify(feeConfigurationRepository).getAllFeesActiveOn(date, pageable);
        verify(feeConfigurationMapper).toResponse(fee1);
        verify(feeConfigurationMapper).toResponse(fee2);
        verifyNoMoreInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateFee_shouldUpdateAndReturnResponse() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequestWithName("Updated admin fee");
        FeeConfiguration existing = aFeeWithId(1L);
        FeeConfiguration saved = aFeeWithId(1L);
        FeeConfigurationResponseDTO response = new FeeConfigurationResponseDTO(
                1L, "Updated admin fee", request.feeType(), request.percentage(),
                request.effectiveFrom(), request.effectiveTo(), true
        );

        when(feeConfigurationRepository.existsByNameAndFeeTypeIgnoreCaseAndIdNot(request.name().trim(), request.feeType().name(), 1L))
                .thenReturn(false);
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(feeConfigurationRepository.save(existing)).thenReturn(saved);
        when(feeConfigurationMapper.toResponse(saved)).thenReturn(response);

        // Act
        FeeConfigurationResponseDTO result = feeConfigurationService.updateFee(1L, request);

        // Assert
        assertEquals(response, result);

        verify(feeConfigurationRepository).existsByNameAndFeeTypeIgnoreCaseAndIdNot(request.name().trim(), request.feeType().name(), 1L);
        verify(feeConfigurationRepository).findById(1L);
        verify(feeConfigurationMapper).updateFromDTO(request, existing);
        verify(feeConfigurationRepository).save(existing);
        verify(feeConfigurationMapper).toResponse(saved);
        verifyNoMoreInteractions(feeConfigurationRepository, feeConfigurationMapper);
    }

    @Test
    void updateFee_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequest();
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(feeConfigurationRepository).findById(1L);
        verifyNoMoreInteractions(feeConfigurationRepository);
        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void updateFee_shouldThrowWhenInvalidDates() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequestWithDates(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );

        FeeConfiguration existing = aFeeWithId(1L);
        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));
        System.out.println(ex.getErrors());

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.INVALID_DATES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verifyNoInteractions(feeConfigurationMapper);
    }

    @Test
    void updateFee_shouldThrowWhenDuplicateExistsExcludingId() {
        // Arrange
        FeeConfigurationRequestDTO request = aFeeRequest();
        FeeConfiguration existing = aFeeWithId(1L);

        when(feeConfigurationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(feeConfigurationRepository.existsByNameAndFeeTypeIgnoreCaseAndIdNot(request.name().trim(), request.feeType().name(), 1L))
                .thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> feeConfigurationService.updateFee(1L, request));
        System.out.println(ex.getErrors());

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(feeConfigurationRepository).existsByNameAndFeeTypeIgnoreCaseAndIdNot(request.name().trim(), request.feeType().name(), 1L);
        verifyNoMoreInteractions(feeConfigurationRepository);
        verifyNoInteractions(feeConfigurationMapper);
    }
}

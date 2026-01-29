package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.application.facade.RiskFactorConfigurationFacade;
import ro.insurance.buildinginsurance.application.mapper.RiskFactorConfigurationMapper;
import ro.insurance.buildinginsurance.application.service.impl.RiskFactorConfigurationService;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.RiskFactorConfigurationTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class RiskFactorConfigurationServiceTest {

    @Mock
    private RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    @Mock
    private RiskFactorConfigurationMapper riskFactorConfigurationMapper;
    @Mock
    private RiskFactorConfigurationFacade riskFactorConfigurationFacade;

    @InjectMocks
    private RiskFactorConfigurationService riskFactorConfigurationService;

    // --------------------- CREATE ---------------------

    @Test
    void createRiskFactorConfiguration_shouldCreateAndReturnResponse() {
        // Arrange
        RiskFactorConfigurationRequestDTO dto = aRiskFactorConfigurationRequestCity();
        RiskFactorConfiguration entity = aRiskFactorConfigurationEntity();
        RiskFactorConfiguration saved = aRiskFactorConfigurationWithId(1L);
        RiskFactorConfigurationResponseDTO response = aRiskFactorConfigurationResponse();

        doNothing().when(riskFactorConfigurationFacade).validateCreateOrThrow(dto);
        when(riskFactorConfigurationMapper.toEntity(dto)).thenReturn(entity);
        when(riskFactorConfigurationRepository.save(entity)).thenReturn(saved);
        when(riskFactorConfigurationMapper.toResponse(saved)).thenReturn(response);

        // Act
        RiskFactorConfigurationResponseDTO result = riskFactorConfigurationService.createRiskFactorConfiguration(dto);

        // Assert
        assertEquals(response, result);

        InOrder inOrder = inOrder(riskFactorConfigurationFacade, riskFactorConfigurationMapper, riskFactorConfigurationRepository);
        inOrder.verify(riskFactorConfigurationFacade).validateCreateOrThrow(dto);
        inOrder.verify(riskFactorConfigurationMapper).toEntity(dto);
        inOrder.verify(riskFactorConfigurationRepository).save(entity);
        inOrder.verify(riskFactorConfigurationMapper).toResponse(saved);

        verifyNoMoreInteractions(riskFactorConfigurationFacade, riskFactorConfigurationMapper, riskFactorConfigurationRepository);
    }

    // --------------------- GET ---------------------

    @Test
    void getAllRiskFactors_shouldReturnMappedPage() {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);

        RiskFactorConfiguration e1 = aRiskFactorConfigurationWithId(1L);
        RiskFactorConfiguration e2 = aRiskFactorConfigurationWithId(2L);

        Page<RiskFactorConfiguration> page = new PageImpl<>(List.of(e1, e2), pageable, 2);

        RiskFactorConfigurationResponseDTO r1 = aRiskFactorConfigurationResponse();
        RiskFactorConfigurationResponseDTO r2 = aRiskFactorConfigurationResponse();

        when(riskFactorConfigurationRepository.findAll(pageable)).thenReturn(page);
        when(riskFactorConfigurationMapper.toResponse(e1)).thenReturn(r1);
        when(riskFactorConfigurationMapper.toResponse(e2)).thenReturn(r2);

        // Act
        Page<RiskFactorConfigurationResponseDTO> result = riskFactorConfigurationService.getAllRiskFactors(pageable);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getTotalElements());
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getContent().size());

        verify(riskFactorConfigurationRepository).findAll(pageable);
        verify(riskFactorConfigurationMapper).toResponse(e1);
        verify(riskFactorConfigurationMapper).toResponse(e2);

        verifyNoInteractions(riskFactorConfigurationFacade);
        verifyNoMoreInteractions(riskFactorConfigurationRepository, riskFactorConfigurationMapper);
    }

    @Test
    void getRiskFactorsForBuilding_shouldReturnMappedList() {
        // Arrange
        Long buildingId = 99L;
        LocalDate effectiveDate = LocalDate.of(2026, 1, 15);

        RiskFactorConfiguration e1 = aRiskFactorConfigurationWithId(1L);
        RiskFactorConfiguration e2 = aRiskFactorConfigurationWithId(2L);

        RiskFactorConfigurationResponseDTO r1 = aRiskFactorConfigurationResponse();
        RiskFactorConfigurationResponseDTO r2 = aRiskFactorConfigurationResponse();

        when(riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(buildingId, effectiveDate)).thenReturn(List.of(e1, e2));
        when(riskFactorConfigurationMapper.toResponse(e1)).thenReturn(r1);
        when(riskFactorConfigurationMapper.toResponse(e2)).thenReturn(r2);

        // Act
        List<RiskFactorConfigurationResponseDTO> result = riskFactorConfigurationService.getRiskFactorsForBuilding(buildingId, effectiveDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(List.of(r1, r2), result);

        verify(riskFactorConfigurationRepository).findRiskFactorsByBuildingIdActiveOn(buildingId, effectiveDate);
        verify(riskFactorConfigurationMapper).toResponse(e1);
        verify(riskFactorConfigurationMapper).toResponse(e2);

        verifyNoInteractions(riskFactorConfigurationFacade);
        verifyNoMoreInteractions(riskFactorConfigurationRepository, riskFactorConfigurationMapper);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateRiskFactorConfiguration_shouldUpdateAndReturnResponse() {
        // Arrange
        Long id = 1L;
        RiskFactorConfigurationRequestDTO dto = aRiskFactorConfigurationRequestCity();

        RiskFactorConfiguration existing = aRiskFactorConfigurationWithId(id);
        RiskFactorConfiguration saved = aRiskFactorConfigurationWithId(id);
        RiskFactorConfigurationResponseDTO response = aRiskFactorConfigurationResponse();

        when(riskFactorConfigurationRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
        doNothing().when(riskFactorConfigurationFacade).validateUpdateOrThrow(id, dto);
        doNothing().when(riskFactorConfigurationMapper).updateFromDTO(dto, existing);
        when(riskFactorConfigurationRepository.save(existing)).thenReturn(saved);
        when(riskFactorConfigurationMapper.toResponse(saved)).thenReturn(response);

        // Act
        RiskFactorConfigurationResponseDTO result = riskFactorConfigurationService.updateRiskFactorConfiguration(id, dto);

        // Assert
        assertEquals(response, result);

        InOrder inOrder = inOrder(riskFactorConfigurationRepository, riskFactorConfigurationFacade, riskFactorConfigurationMapper);
        inOrder.verify(riskFactorConfigurationRepository).findById(id);
        inOrder.verify(riskFactorConfigurationFacade).validateUpdateOrThrow(id, dto);
        inOrder.verify(riskFactorConfigurationMapper).updateFromDTO(dto, existing);
        inOrder.verify(riskFactorConfigurationRepository).save(existing);
        inOrder.verify(riskFactorConfigurationMapper).toResponse(saved);

        verifyNoMoreInteractions(riskFactorConfigurationRepository, riskFactorConfigurationFacade, riskFactorConfigurationMapper);
    }

    @Test
    void updateRiskFactorConfiguration_shouldThrowWhenNotFound() {
        // Arrange
        Long id = 999L;
        RiskFactorConfigurationRequestDTO dto = aRiskFactorConfigurationRequestCity();

        when(riskFactorConfigurationRepository.findById(id)).thenReturn(java.util.Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> riskFactorConfigurationService.updateRiskFactorConfiguration(id, dto));

        // Assert
        assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.RISK_FACTOR_CONFIGURATION_NOT_FOUND.name().equals(e.getCode())));

        verify(riskFactorConfigurationRepository).findById(id);
        verifyNoInteractions(riskFactorConfigurationFacade, riskFactorConfigurationMapper);
        verifyNoMoreInteractions(riskFactorConfigurationRepository);
    }

}

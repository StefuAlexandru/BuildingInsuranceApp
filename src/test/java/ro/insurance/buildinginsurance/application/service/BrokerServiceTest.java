package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ro.insurance.buildinginsurance.application.mapper.BrokerMapper;
import ro.insurance.buildinginsurance.application.service.impl.BrokerService;
import ro.insurance.buildinginsurance.domain.entity.Broker;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BrokerRepository;
import ro.insurance.buildinginsurance.util.BrokerTestDataFactory;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokerServiceTest {

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerMapper brokerMapper;

    @InjectMocks
    private BrokerService brokerService;

    // --------------------- CREATE ---------------------

    @Test
    void createBroker_shouldSaveAndReturnResponse_whenCodeNotExists() {
        BrokerRequestDTO req = mock(BrokerRequestDTO.class);
        when(req.brokerCode()).thenReturn("BRK-001");

        Broker toSave = BrokerTestDataFactory.anInactiveBrokerWithId(null);
        toSave.setBrokerCode("BRK-001");

        Broker saved = BrokerTestDataFactory.aBrokerWithId(10L);
        saved.setBrokerCode("BRK-001");

        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.existsByBrokerCodeIgnoreCase("BRK-001")).thenReturn(false);
        when(brokerMapper.toEntity(req)).thenReturn(toSave);
        when(brokerRepository.save(toSave)).thenReturn(saved);
        when(brokerMapper.toResponse(saved)).thenReturn(resp);

        BrokerResponseDTO result = brokerService.createBroker(req);

        assertSame(resp, result);
        verify(brokerRepository).existsByBrokerCodeIgnoreCase("BRK-001");
        verify(brokerMapper).toEntity(req);
        verify(brokerRepository).save(toSave);
        verify(brokerMapper).toResponse(saved);
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    @Test
    void createBroker_shouldThrow_whenCodeAlreadyExists() {
        BrokerRequestDTO req = mock(BrokerRequestDTO.class);
        when(req.brokerCode()).thenReturn("BRK-001");

        when(brokerRepository.existsByBrokerCodeIgnoreCase("BRK-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> brokerService.createBroker(req));

        verify(brokerRepository).existsByBrokerCodeIgnoreCase("BRK-001");
        verifyNoMoreInteractions(brokerRepository);
        verifyNoInteractions(brokerMapper);
    }

    // --------------------- ACTIVATE ---------------------

    @Test
    void activateBroker_shouldThrow_whenNotFound() {
        when(brokerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> brokerService.activateBroker(1L));

        verify(brokerRepository).findById(1L);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoInteractions(brokerMapper);
    }

    @Test
    void activateBroker_shouldReturnMapped_withoutSave_whenAlreadyActive() {
        Broker broker = BrokerTestDataFactory.aBrokerWithId(1L);
        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(broker));
        when(brokerMapper.toResponse(broker)).thenReturn(resp);

        BrokerResponseDTO result = brokerService.activateBroker(1L);

        assertSame(resp, result);
        verify(brokerRepository).findById(1L);
        verify(brokerMapper).toResponse(broker);
        verify(brokerRepository, never()).save(any());
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    @Test
    void activateBroker_shouldSetActiveTrue_andSave_whenInactive() {
        Broker broker = BrokerTestDataFactory.anInactiveBrokerWithId(1L);
        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(broker));
        when(brokerRepository.save(any(Broker.class))).thenAnswer(inv -> inv.getArgument(0));
        when(brokerMapper.toResponse(any(Broker.class))).thenReturn(resp);

        BrokerResponseDTO result = brokerService.activateBroker(1L);

        assertSame(resp, result);
        assertTrue(broker.isActive(), "Broker should be activated");

        ArgumentCaptor<Broker> captor = ArgumentCaptor.forClass(Broker.class);
        verify(brokerRepository).findById(1L);
        verify(brokerRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
        verify(brokerMapper).toResponse(any(Broker.class));
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    // --------------------- DEACTIVATE ---------------------

    @Test
    void deactivateBroker_shouldReturnMapped_withoutSave_whenAlreadyInactive() {
        Broker broker = BrokerTestDataFactory.anInactiveBrokerWithId(1L);
        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(broker));
        when(brokerMapper.toResponse(broker)).thenReturn(resp);

        BrokerResponseDTO result = brokerService.deactivateBroker(1L);

        assertSame(resp, result);
        verify(brokerRepository).findById(1L);
        verify(brokerMapper).toResponse(broker);
        verify(brokerRepository, never()).save(any());
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    @Test
    void deactivateBroker_shouldSetActiveFalse_andSave_whenActive() {
        Broker broker = BrokerTestDataFactory.aBrokerWithId(1L);
        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(broker));
        when(brokerRepository.save(any(Broker.class))).thenAnswer(inv -> inv.getArgument(0));
        when(brokerMapper.toResponse(any(Broker.class))).thenReturn(resp);

        BrokerResponseDTO result = brokerService.deactivateBroker(1L);

        assertSame(resp, result);
        assertFalse(broker.isActive(), "Broker should be deactivated");

        verify(brokerRepository).findById(1L);
        verify(brokerRepository).save(broker);
        verify(brokerMapper).toResponse(any(Broker.class));
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    // --------------------- GET ---------------------

    @Test
    void getBroker_shouldThrow_whenNotFound() {
        when(brokerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> brokerService.getBroker(1L));

        verify(brokerRepository).findById(1L);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoInteractions(brokerMapper);
    }

    @Test
    void getBroker_shouldReturnMapped_whenFound() {
        Broker broker = BrokerTestDataFactory.aBrokerWithId(1L);
        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(broker));
        when(brokerMapper.toResponse(broker)).thenReturn(resp);

        BrokerResponseDTO result = brokerService.getBroker(1L);

        assertSame(resp, result);
        verify(brokerRepository).findById(1L);
        verify(brokerMapper).toResponse(broker);
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    @Test
    void getBrokers_shouldMapPage() {
        Broker b1 = BrokerTestDataFactory.aBrokerWithId(1L);
        Broker b2 = BrokerTestDataFactory.anInactiveBrokerWithId(2L);

        BrokerResponseDTO r1 = mock(BrokerResponseDTO.class);
        BrokerResponseDTO r2 = mock(BrokerResponseDTO.class);

        PageRequest pageable = PageRequest.of(0, 2);
        Page<Broker> page = new PageImpl<>(List.of(b1, b2), pageable, 2);

        when(brokerRepository.findAll(pageable)).thenReturn(page);
        when(brokerMapper.toResponse(b1)).thenReturn(r1);
        when(brokerMapper.toResponse(b2)).thenReturn(r2);

        Page<BrokerResponseDTO> result = brokerService.getBrokers(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(r1, r2), result.getContent());

        verify(brokerRepository).findAll(pageable);
        verify(brokerMapper).toResponse(b1);
        verify(brokerMapper).toResponse(b2);
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateBroker_shouldUpdateAndReturnResponse_whenUniqueCode() {
        // Arrange
        Broker existing = BrokerTestDataFactory.aBrokerWithId(1L);

        BrokerRequestDTO req = new BrokerRequestDTO(
                "NEW-CODE", "New Name", "new@email.com", "0711111111", existing.getCommissionPct()
        );

        BrokerResponseDTO resp = mock(BrokerResponseDTO.class);

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(brokerRepository.existsByBrokerCodeIgnoreCase("NEW-CODE")).thenReturn(false);
        when(brokerRepository.save(existing)).thenReturn(existing);
        when(brokerMapper.toResponse(existing)).thenReturn(resp);

        // Act
        BrokerResponseDTO result = brokerService.updateBroker(1L, req);

        // Assert
        assertSame(resp, result);

        verify(brokerRepository).findById(1L);
        verify(brokerRepository).existsByBrokerCodeIgnoreCase("NEW-CODE");
        verify(brokerMapper).updateFromDto(req, existing);
        verify(brokerRepository).save(existing);
        verify(brokerMapper).toResponse(existing);
        verifyNoMoreInteractions(brokerRepository, brokerMapper);
    }

    @Test
    void updateBroker_shouldThrow_whenBrokerCodeAlreadyExists() {
        // Arrange
        Broker existing = BrokerTestDataFactory.aBrokerWithId(1L);

        BrokerRequestDTO req = new BrokerRequestDTO(
                "DUP-CODE", "New Name", "new@email.com", "0711111111", existing.getCommissionPct()
        );

        when(brokerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(brokerRepository.existsByBrokerCodeIgnoreCase("DUP-CODE")).thenReturn(true);

        // Act + Assert
        assertThrows(BusinessException.class, () -> brokerService.updateBroker(1L, req));

        verify(brokerRepository).findById(1L);
        verify(brokerRepository).existsByBrokerCodeIgnoreCase("DUP-CODE");
        verifyNoMoreInteractions(brokerRepository);
        verifyNoInteractions(brokerMapper);
    }

}

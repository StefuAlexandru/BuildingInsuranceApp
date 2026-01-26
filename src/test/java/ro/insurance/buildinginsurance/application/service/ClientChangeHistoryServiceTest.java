package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.service.impl.ClientChangeHistoryService;
import ro.insurance.buildinginsurance.domain.entity.ClientChangeHistory;
import ro.insurance.buildinginsurance.domain.enums.ClientType;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientChangeHistoryRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClientChangeHistoryServiceTest {

    @Mock
    private ClientChangeHistoryRepository repo;

    @InjectMocks
    private ClientChangeHistoryService service;

    @Test
    void logClientTypeChange_shouldNotSaveWhenTypesEqual() {
        service.logClientTypeChange(1L, ClientType.COMPANY, ClientType.COMPANY, "reason", "alex");
        verifyNoInteractions(repo);
    }

    @Test
    void logClientTypeChange_shouldSaveWhenTypesDifferent() {
        service.logClientTypeChange(1L, ClientType.COMPANY, ClientType.INDIVIDUAL, "  reason  ", "alex");

        ArgumentCaptor<ClientChangeHistory> captor = ArgumentCaptor.forClass(ClientChangeHistory.class);
        verify(repo).save(captor.capture());

        ClientChangeHistory saved = captor.getValue();
        assertAll(
                () -> assertEquals(1L, saved.getClientId()),
                () -> assertEquals(ClientChangeHistory.Field.CLIENT_TYPE, saved.getField()),
                () -> assertEquals("COMPANY", saved.getOldValue()),
                () -> assertEquals("INDIVIDUAL", saved.getNewValue()),
                () -> assertEquals("reason", saved.getReason()),
                () -> assertEquals("alex", saved.getChangedBy())
        );
    }

    @Test
    void logIdentificationNumberChange_shouldNotSaveWhenIdsEqual() {
        service.logIdentificationNumberChange(1L, "ID1", "ID1", "reason", "alex");
        verifyNoInteractions(repo);
    }

    @Test
    void logIdentificationNumberChange_shouldSaveWhenIdsDifferent() {
        service.logIdentificationNumberChange(1L, "OLD", "NEW", "  update  ", "alex");

        ArgumentCaptor<ClientChangeHistory> captor = ArgumentCaptor.forClass(ClientChangeHistory.class);
        verify(repo).save(captor.capture());

        ClientChangeHistory saved = captor.getValue();
        assertAll(
                () -> assertEquals(1L, saved.getClientId()),
                () -> assertEquals(ClientChangeHistory.Field.IDENTIFICATION_NUMBER, saved.getField()),
                () -> assertEquals("OLD", saved.getOldValue()),
                () -> assertEquals("NEW", saved.getNewValue()),
                () -> assertEquals("update", saved.getReason()),
                () -> assertEquals("alex", saved.getChangedBy())
        );
    }
}

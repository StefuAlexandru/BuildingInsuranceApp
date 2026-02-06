package ro.insurance.buildinginsurance.application.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientIdentityPatchDTO;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.ClientType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientRepository;
import ro.insurance.buildinginsurance.application.service.IClientChangeHistoryService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientIdentityPatchDTOCustom;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientIndividual;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;

@ExtendWith(MockitoExtension.class)
class ClientIdentityFacadeTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private IClientChangeHistoryService historyService;

    @InjectMocks
    private ClientIdentityFacade facade;

    @Test
    void patchIdentity_shouldThrowWhenChangedByBlank() {
        // Arrange
        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(ClientType.COMPANY, "ID2", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "   "));

        assertHasError(ex,ErrorCode.CHANGED_BY_REQUIRED);

        verifyNoInteractions(clientRepository, historyService);
    }

    @Test
    void patchIdentity_shouldThrowWhenClientNotFound() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        ClientIdentityPatchDTO clientIdentityPatchDTO = aClientIdentityPatchDTOCustom(ClientType.COMPANY, "ID2", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, clientIdentityPatchDTO, "alex"));

        assertHasError(ex,ErrorCode.CLIENT_NOT_FOUND);

        verify(clientRepository).findById(1L);
        verifyNoInteractions(historyService);
    }

    @Test
    void patchIdentity_shouldThrowWhenNoChanges() {
        // Arrange
        Client client = aClientIndividual();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientIdentityPatchDTO dto =aClientIdentityPatchDTOCustom(null, null, "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "alex"));

        assertHasError(ex,ErrorCode.NO_CHANGES);

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(historyService);
    }

    @Test
    void patchIdentity_shouldThrowWhenTypeChangesWithoutIdNumber() {
        // Arrange
        Client client = aClientIndividual();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(ClientType.COMPANY, null, "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "alex"));

        assertHasError(ex,ErrorCode.ID_NUMBER_REQUIRED_WHEN_TYPE_CHANGES);

        verify(clientRepository).findById(1L);
        verifyNoInteractions(historyService);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void patchIdentity_shouldThrowWhenNewIdAlreadyExists() {
        // Arrange
        Client client = aClientIndividual();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByIdentificationNumberAndIdNot("ID2", 1L)).thenReturn(true);

        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(null, "ID2", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "alex"));

        assertHasError(ex,ErrorCode.CLIENT_ALREADY_EXIST);

        verify(clientRepository).findById(1L);
        verify(clientRepository).existsByIdentificationNumberAndIdNot("ID2", 1L);
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(historyService);
    }

    @Test
    void patchIdentity_shouldLogChangesAndSave() {
        // Arrange
        Client client = aClientIndividual();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByIdentificationNumberAndIdNot("ID2", 1L)).thenReturn(false);

        Client saved = new Client();
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientIdentityPatchDTO dto =aClientIdentityPatchDTOCustom(ClientType.COMPANY, "ID2", "reason");

        // Act
        Client result = facade.patchIdentity(1L, dto, "  alex  ");

        // Assert
        assertAll(
                () -> assertSame(saved, result),
                () -> assertEquals(ClientType.COMPANY, client.getClientType()),
                () -> assertEquals("ID2", client.getIdentificationNumber())
        );

        verify(historyService).logClientTypeChange(1L, ClientType.INDIVIDUAL, ClientType.COMPANY, "reason", "alex");
        verify(historyService).logIdentificationNumberChange(1L, "ID1", "ID2", "reason", "alex");
        verify(clientRepository).save(client);
    }

    @Test
    void patchIdentity_shouldThrowWhenChangedByNull() {
        // Arrange
        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(ClientType.COMPANY, "ID2", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, null));

        assertHasError(ex,ErrorCode.CHANGED_BY_REQUIRED);

        verifyNoInteractions(clientRepository, historyService);
    }

    @Test
    void patchIdentity_shouldTreatBlankNewIdentificationNumberAsNoChange() {
        // Arrange
        Client client = aClientIndividual();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(null, "   ", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "alex"));

        assertHasError(ex,ErrorCode.NO_CHANGES);

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(historyService);
    }

    @Test
    void patchIdentity_shouldLogOnlyIdentificationChange_whenOnlyIdChanges() {
        // Arrange
        Client client = aClientIndividual();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.existsByIdentificationNumberAndIdNot("ID2", 1L)).thenReturn(false);

        Client saved = new Client();
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(null, "ID2", "reason");

        // Act
        Client result = facade.patchIdentity(1L, dto, "alex");

        // Assert
        assertAll(
                () -> assertSame(saved, result),
                () -> assertEquals(ClientType.INDIVIDUAL, client.getClientType()),
                () -> assertEquals("ID2", client.getIdentificationNumber())
        );

        verify(historyService, never()).logClientTypeChange(anyLong(), any(), any(), any(), any());
        verify(historyService).logIdentificationNumberChange(1L, "ID1", "ID2", "reason", "alex");
        verify(clientRepository).existsByIdentificationNumberAndIdNot("ID2", 1L);
        verify(clientRepository).save(client);
    }

    @Test
    void patchIdentity_shouldThrowWhenTypeChangesButIdStaysSame_afterTrim() {
        // Arrange
        Client client = aClientIndividual();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientIdentityPatchDTO dto = aClientIdentityPatchDTOCustom(ClientType.COMPANY, "  ID1  ", "reason");

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.patchIdentity(1L, dto, "alex"));

        assertHasError(ex,ErrorCode.ID_NUMBER_REQUIRED_WHEN_TYPE_CHANGES);

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(historyService);
    }











}
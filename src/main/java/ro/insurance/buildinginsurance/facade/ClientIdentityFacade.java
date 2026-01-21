package ro.insurance.buildinginsurance.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.clientDto.ClientIdentityPatchDTO;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.repository.ClientRepository;
import ro.insurance.buildinginsurance.service.IClientChangeHistoryService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientIdentityFacade {

    private final ClientRepository clientRepository;
    private final IClientChangeHistoryService historyService;

    public Client patchIdentity(Long clientId, ClientIdentityPatchDTO clientIdentityPatchDTO, String changedBy) {
        String actor = normalizeActor(changedBy);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));

        ClientType newType = resolveNewType(client, clientIdentityPatchDTO);

        String newIdentificationNumber = resolveNewIdentificationNumber(client, clientIdentityPatchDTO);

        boolean typeActuallyChanges = newType != client.getClientType();

        String normalizedNewId = trimToNull(clientIdentityPatchDTO.newIdentificationNumber());
        boolean identificationNumberActuallyChanges = normalizedNewId != null && !Objects.equals(normalizedNewId, client.getIdentificationNumber());

        validateUniqueness(client, newIdentificationNumber, identificationNumberActuallyChanges);

        validatePatchRequest(typeActuallyChanges, identificationNumberActuallyChanges);

        if (typeActuallyChanges) {
            historyService.logClientTypeChange(clientId, client.getClientType(), newType, clientIdentityPatchDTO.reason(), actor);
            client.setClientType(newType);
        }

        historyService.logIdentificationNumberChange(clientId, client.getIdentificationNumber(), newIdentificationNumber, clientIdentityPatchDTO.reason(), actor);

        client.setIdentificationNumber(newIdentificationNumber);

        return clientRepository.save(client);
    }

    private String normalizeActor(String changedBy) {
        String actor = changedBy == null ? "" : changedBy.trim();
        if (actor.isBlank()) throw new BusinessException(ErrorCode.CHANGED_BY_REQUIRED);
        return actor;
    }

    private void validatePatchRequest(boolean typeActuallyChanges, boolean identificationNumberActuallyChanges) {
        if (!typeActuallyChanges && !identificationNumberActuallyChanges) {
            throw new BusinessException(ErrorCode.NO_CHANGES);
        }
        if (typeActuallyChanges && !identificationNumberActuallyChanges) {
            throw new BusinessException(ErrorCode.ID_NUMBER_REQUIRED_WHEN_TYPE_CHANGES);
        }
    }

    private void validateUniqueness(Client client, String newId, boolean identificationNumberActuallyChanges) {
        if (!identificationNumberActuallyChanges) return;

        if (clientRepository.existsByIdentificationNumberAndIdNot(newId, client.getId())) {
            throw new BusinessException(ErrorCode.CLIENT_ALREADY_EXIST);
        }
    }

    private ClientType resolveNewType(Client client, ClientIdentityPatchDTO clientIdentityPatchDTO) {
        return clientIdentityPatchDTO.newClientType() != null
                ? clientIdentityPatchDTO.newClientType()
                : client.getClientType();
    }

    private String resolveNewIdentificationNumber(Client client, ClientIdentityPatchDTO clientIdentityPatchDTO) {
        String raw = clientIdentityPatchDTO.newIdentificationNumber();
        return (raw != null && !raw.isBlank()) ? raw.trim() : client.getIdentificationNumber();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

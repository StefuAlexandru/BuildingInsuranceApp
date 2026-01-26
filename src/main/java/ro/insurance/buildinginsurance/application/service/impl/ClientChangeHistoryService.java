package ro.insurance.buildinginsurance.application.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.domain.entity.ClientChangeHistory;
import ro.insurance.buildinginsurance.domain.enums.ClientType;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientChangeHistoryRepository;
import ro.insurance.buildinginsurance.application.service.IClientChangeHistoryService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClientChangeHistoryService implements IClientChangeHistoryService {

    private final ClientChangeHistoryRepository clientChangeHistoryRepository;

    public void logClientTypeChange(Long clientId, ClientType oldType, ClientType newType, String reason, String actor) {
        if (oldType == newType) return;

        clientChangeHistoryRepository.save(build(clientId, ClientChangeHistory.Field.CLIENT_TYPE, oldType.name(), newType.name(), reason, actor));

        log.debug("Client history saved: clientId={}, field={}, {} -> {}, actor={}", clientId, ClientChangeHistory.Field.CLIENT_TYPE, oldType, newType, actor);
    }

    public void logIdentificationNumberChange(Long clientId, String oldId, String newId, String reason, String actor) {
        if (oldId.equals(newId)) return;

        clientChangeHistoryRepository.save(build(clientId, ClientChangeHistory.Field.IDENTIFICATION_NUMBER, oldId, newId, reason, actor));

        log.debug("Client history saved: clientId={}, field={}, oldId='{}' -> newId='{}', actor={}", clientId, ClientChangeHistory.Field.IDENTIFICATION_NUMBER, oldId, newId, actor);
    }

    private ClientChangeHistory build(
            Long clientId,
            ClientChangeHistory.Field field,
            String oldValue,
            String newValue,
            String reason,
            String actor
    ) {
        return getClientChangeHistory(clientId, field, oldValue, newValue, reason, actor);
    }

    @NonNull
    private static ClientChangeHistory getClientChangeHistory(
            Long clientId,
            ClientChangeHistory.Field field,
            String oldValue,
            String newValue,
            String reason,
            String actor
    ) {
        ClientChangeHistory clientChangeHistory = new ClientChangeHistory();
        clientChangeHistory.setClientId(clientId);
        clientChangeHistory.setField(field);
        clientChangeHistory.setOldValue(oldValue);
        clientChangeHistory.setNewValue(newValue);
        clientChangeHistory.setReason(reason.trim());
        clientChangeHistory.setChangedBy(actor);
        return clientChangeHistory;
    }
}


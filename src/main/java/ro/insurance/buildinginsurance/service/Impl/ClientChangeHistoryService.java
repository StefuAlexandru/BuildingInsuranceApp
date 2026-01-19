package ro.insurance.buildinginsurance.service.Impl;


import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.entity.ClientChangeHistory;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.repository.ClientChangeHistoryRepository;
import ro.insurance.buildinginsurance.service.IClientChangeHistoryService;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientChangeHistoryService implements IClientChangeHistoryService {

    private final ClientChangeHistoryRepository clientChangeHistoryRepository;

    public void logClientTypeChange(Long clientId, ClientType oldType, ClientType newType, String reason, String actor) {
        if (oldType == newType) return;

        clientChangeHistoryRepository.save(build(clientId, ClientChangeHistory.Field.CLIENT_TYPE, oldType.name(), newType.name(), reason, actor));
    }

    public void logIdentificationNumberChange(Long clientId, String oldId, String newId, String reason, String actor) {
        if (oldId.equals(newId)) return;

        clientChangeHistoryRepository.save(build(clientId, ClientChangeHistory.Field.IDENTIFICATION_NUMBER, oldId, newId, reason, actor));
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


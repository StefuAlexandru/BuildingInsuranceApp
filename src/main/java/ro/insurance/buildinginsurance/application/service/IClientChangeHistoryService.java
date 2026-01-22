package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.domain.enums.ClientType;

public interface IClientChangeHistoryService{
    void logClientTypeChange(Long clientId, ClientType clientType, ClientType newType, String reason, String actor);
    void logIdentificationNumberChange(Long clientId, String identificationNumber, String newId, String reason, String actor);
}

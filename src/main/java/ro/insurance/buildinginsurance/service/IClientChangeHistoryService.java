package ro.insurance.buildinginsurance.service;

import ro.insurance.buildinginsurance.enums.ClientType;

public interface IClientChangeHistoryService{
    void logClientTypeChange(Long clientId, ClientType clientType, ClientType newType, String reason, String actor);
    void logIdentificationNumberChange(Long clientId, String identificationNumber, String newId, String reason, String actor);
}

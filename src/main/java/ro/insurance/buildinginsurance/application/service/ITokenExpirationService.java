package ro.insurance.buildinginsurance.application.service;

public interface ITokenExpirationService {
    int deleteExpiredTokens();
}

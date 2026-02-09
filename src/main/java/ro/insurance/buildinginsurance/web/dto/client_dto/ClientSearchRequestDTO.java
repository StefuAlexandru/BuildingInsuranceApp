package ro.insurance.buildinginsurance.web.dto.client_dto;

public record ClientSearchRequestDTO(
        String name,
        String identificationNumber
) {}


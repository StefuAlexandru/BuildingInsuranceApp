package ro.insurance.buildinginsurance.web.dto.auth_dto;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken
) {}
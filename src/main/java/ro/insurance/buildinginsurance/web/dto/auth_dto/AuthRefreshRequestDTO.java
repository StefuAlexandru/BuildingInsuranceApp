package ro.insurance.buildinginsurance.web.dto.auth_dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequestDTO(
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken
) {}
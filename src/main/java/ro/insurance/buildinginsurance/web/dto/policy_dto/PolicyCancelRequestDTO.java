package ro.insurance.buildinginsurance.web.dto.policy_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyCancelRequestDTO(
        @NotBlank(message = "Cancellation reason cannot be blank")
        @Size(max = 255, message = "Cancellation reason must be at most 255 characters")
        String cancellationReason
) { }

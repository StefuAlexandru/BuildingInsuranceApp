package ro.insurance.buildinginsurance.web.dto.policyDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record PolicyCancelRequestDTO(
        @NotBlank(message = "Cancellation reason cannot be blank")
        @Size(max = 255, message = "Cancellation reason must be at most 255 characters")
        String cancellationReason,

        OffsetDateTime cancelledAt
) { }

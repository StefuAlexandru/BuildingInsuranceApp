package ro.insurance.buildinginsurance.web.dto.policyDto;

import java.time.OffsetDateTime;

public record PolicyActivateRequestDTO(
    OffsetDateTime activatedAt
) { }

package ro.insurance.buildinginsurance.web.dto.policyCalculationDto;

import java.math.BigDecimal;
import java.util.List;

public record PolicyCalculationResponseDTO(
        BigDecimal finalPremium,
        BigDecimal totalPercent,
        List<AppliedAdjustment> breakdown
) {}

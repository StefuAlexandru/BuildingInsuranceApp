package ro.insurance.buildinginsurance.web.dto.policy_calculation_dto;

import java.math.BigDecimal;
import java.util.List;

public record PolicyCalculationResponseDTO(
        BigDecimal finalPremium,
        BigDecimal totalPercent,
        List<AppliedAdjustment> breakdown
) {}

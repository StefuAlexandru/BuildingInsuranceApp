package ro.insurance.buildinginsurance.web.dto.policyCalculationDto;

import java.math.BigDecimal;

public record AppliedAdjustment(
        String type,
        String code,
        BigDecimal percent
) {}
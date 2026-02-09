package ro.insurance.buildinginsurance.web.dto.policy_calculation_dto;

import java.math.BigDecimal;

public record AppliedAdjustment(
        String type,
        String code,
        BigDecimal percent
) {}
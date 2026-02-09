package ro.insurance.buildinginsurance.web.dto.report_dto;

import java.math.BigDecimal;

public record ReportTotalsDTO(
        long policyCount,
        BigDecimal totalPremium,
        BigDecimal totalInsuredValue
) {}

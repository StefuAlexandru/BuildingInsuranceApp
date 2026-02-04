package ro.insurance.buildinginsurance.web.dto.reportDto;

import java.math.BigDecimal;

public record ReportTotalsDTO(
        long policyCount,
        BigDecimal totalPremium,
        BigDecimal totalInsuredValue
) {}

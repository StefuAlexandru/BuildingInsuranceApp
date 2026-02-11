package ro.insurance.buildinginsurance.web.dto.report_dto;

import java.math.BigDecimal;

public record ReportRowDTO(
        String groupKey,
        String groupName,
        Long currencyId,
        String currencyCode,
        long policyCount,
        BigDecimal totalFinalPremium,        // in currency of the report
        BigDecimal totalFinalPremiumInBaseCurrency    // in base currency (e.g., RON)
) {}
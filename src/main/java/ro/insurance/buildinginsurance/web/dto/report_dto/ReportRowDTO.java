package ro.insurance.buildinginsurance.web.dto.report_dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportRowDTO(
        String groupKey,
        String groupName,
        LocalDate periodStart,          // null when granularity == NONE
        long policyCount,
        BigDecimal totalPremium,
        BigDecimal totalInsuredValue
) {}
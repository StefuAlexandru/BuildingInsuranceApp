package ro.insurance.buildinginsurance.web.dto.report_dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.domain.enums.PeriodGranularity;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.time.LocalDate;
import java.util.List;

public record ReportRequestDTO(
        @NotNull(message = "From date cannot be null")
        LocalDate from,
        @NotNull(message = "To date cannot be null")
        LocalDate to,

        @NotNull(message = "Period granularity cannot pe null")
        PeriodGranularity granularity,

        @Size(min = 1, message = "Group by must have at least one value")
        @NotNull(message = "Group by cannot be null")
        List<GroupBy> groupBy,

        PolicyStatus status,
        Long currencyId,
        Long brokerId
) {}


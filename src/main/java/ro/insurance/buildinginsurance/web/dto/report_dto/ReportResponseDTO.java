package ro.insurance.buildinginsurance.web.dto.report_dto;

import java.util.List;

public record ReportResponseDTO(
        List<ReportRowDTO> rows,
        ReportTotalsDTO totals
) {}


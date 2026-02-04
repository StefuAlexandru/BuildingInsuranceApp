package ro.insurance.buildinginsurance.web.dto.reportDto;

import java.util.List;

public record ReportResponseDTO(
        List<ReportRowDTO> rows,
        ReportTotalsDTO totals
) {}


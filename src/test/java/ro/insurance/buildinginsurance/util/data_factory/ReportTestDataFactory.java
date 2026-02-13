package ro.insurance.buildinginsurance.util.data_factory;

import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;

import java.time.LocalDate;

public class ReportTestDataFactory {

    private ReportTestDataFactory() {}

    private static final LocalDate FROM = LocalDate.of(2026, 2, 4);
    private static final LocalDate TO   = LocalDate.of(2027, 2, 4);

    public static ReportRequestDTO aReportReq_minRequired(Long currencyId) {
        return new ReportRequestDTO(
                FROM, TO, currencyId,
                null, null
        );
    }
    public static ReportRequestDTO aReportReq_invalid_fromAfterTo(Long currencyId) {
        return new ReportRequestDTO(
                LocalDate.of(2027, 2, 4),
                LocalDate.of(2026, 2, 4),
                currencyId, null, null
        );
    }
}

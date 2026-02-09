package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.enums.PeriodGranularity;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;

import java.time.LocalDate;
import java.util.List;

public class ReportTestDataFactory {

    private ReportTestDataFactory() {}

    private static final LocalDate FROM = LocalDate.of(2026, 2, 4);
    private static final LocalDate TO   = LocalDate.of(2027, 2, 4);

    public static ReportRequestDTO aReportReq_countryCityBroker_none() {
        return new ReportRequestDTO(
                FROM, TO,
                PeriodGranularity.NONE,
                List.of(GroupBy.COUNTRY, GroupBy.CITY, GroupBy.BROKER),
                null, null, null
        );
    }
    public static ReportRequestDTO aReportReq_countryCity_day() {
        return new ReportRequestDTO(
                FROM, TO,
                PeriodGranularity.DAY,
                List.of(GroupBy.COUNTRY, GroupBy.CITY),
                null, null, null
        );
    }
    public static ReportRequestDTO aReportReq_fullFilters_month(Long currencyId, Long brokerId) {
        return new ReportRequestDTO(
                FROM, TO,
                PeriodGranularity.MONTH,
                List.of(GroupBy.COUNTRY, GroupBy.CITY, GroupBy.BROKER),
                PolicyStatus.ACTIVE,
                currencyId,
                brokerId
        );
    }

    public static ReportRequestDTO aReportReq_invalid_fromAfterTo() {
        return new ReportRequestDTO(
                LocalDate.of(2027, 2, 4),
                LocalDate.of(2026, 2, 4),
                PeriodGranularity.NONE,
                List.of(GroupBy.COUNTRY),
                null, null, null
        );
    }
}

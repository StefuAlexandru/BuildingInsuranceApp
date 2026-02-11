package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRowDTO;

import java.util.List;

public interface IReportService {
    List<ReportRowDTO> getPoliciesReport(GroupBy groupBy, ReportRequestDTO request);
}



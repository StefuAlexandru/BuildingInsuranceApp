package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportResponseDTO;

public interface IReportService {
    ReportResponseDTO getPoliciesReport(ReportRequestDTO request);
}



package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.web.dto.reportDto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.reportDto.ReportResponseDTO;

public interface IReportService {
    ReportResponseDTO getPoliciesReport(ReportRequestDTO request);
}



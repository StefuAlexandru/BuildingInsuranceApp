package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.insurance.buildinginsurance.application.service.IReportService;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportResponseDTO;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportsController {

    private final IReportService reportService;

    @PostMapping("/policies")
    public ResponseEntity<ReportResponseDTO> getPoliciesReport(@Valid @RequestBody ReportRequestDTO request) {
        return ResponseEntity.ok(reportService.getPoliciesReport(request));
    }
}

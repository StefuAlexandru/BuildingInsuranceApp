package ro.insurance.buildinginsurance.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.insurance.buildinginsurance.application.service.IReportService;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.GroupBy;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRequestDTO;
import ro.insurance.buildinginsurance.web.dto.report_dto.ReportRowDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportController {

    private final IReportService reportService;

    @GetMapping("/policies-by-country")
    public ResponseEntity<List<ReportRowDTO>> policiesByCountry(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam("currency") Long currencyId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        ReportRequestDTO request = new ReportRequestDTO(from, to, currencyId, status, buildingType);
        return ResponseEntity.ok(reportService.getPoliciesReport(GroupBy.COUNTRY, request));
    }

    @GetMapping("/policies-by-county")
    public ResponseEntity<List<ReportRowDTO>> policiesByCounty(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam("currency") Long currencyId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        ReportRequestDTO request = new ReportRequestDTO(from, to, currencyId, status, buildingType);
        return ResponseEntity.ok(reportService.getPoliciesReport(GroupBy.COUNTY, request));
    }

    @GetMapping("/policies-by-city")
    public ResponseEntity<List<ReportRowDTO>> policiesByCity(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam("currency") Long currencyId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        ReportRequestDTO request = new ReportRequestDTO(from, to, currencyId, status, buildingType);
        return ResponseEntity.ok(reportService.getPoliciesReport(GroupBy.CITY, request));
    }

    @GetMapping("/policies-by-broker")
    public ResponseEntity<List<ReportRowDTO>> policiesByBroker(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam("currency") Long currencyId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) BuildingType buildingType
    ) {
        ReportRequestDTO request = new ReportRequestDTO(from, to, currencyId, status, buildingType);
        return ResponseEntity.ok(reportService.getPoliciesReport(GroupBy.BROKER, request));
    }
}

package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IRiskFactorConfigurationService;
import ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto.RiskFactorConfigurationResponseDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-factors")
public class RiskFactorConfigurationController {

    private final IRiskFactorConfigurationService riskFactorConfigurationService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<RiskFactorConfigurationResponseDTO> create(@Valid @RequestBody RiskFactorConfigurationRequestDTO dto) {
        RiskFactorConfigurationResponseDTO created = riskFactorConfigurationService.createRiskFactorConfiguration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ----------------------  GET  ----------------------

    @GetMapping
    public ResponseEntity<Page<RiskFactorConfigurationResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(riskFactorConfigurationService.getAllRiskFactors(pageable));
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<RiskFactorConfigurationResponseDTO>> getForBuilding(
            @PathVariable Long buildingId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(riskFactorConfigurationService.getRiskFactorsForBuilding(buildingId, effectiveDate));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{id}")
    public ResponseEntity<RiskFactorConfigurationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskFactorConfigurationRequestDTO dto
    ) {
        return ResponseEntity.ok(riskFactorConfigurationService.updateRiskFactorConfiguration(id, dto));
    }
}

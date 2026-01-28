package ro.insurance.buildinginsurance.web.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IFeeConfigurationService;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationResponseDTO;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/fees")
public class FeeConfigurationController {

    private final IFeeConfigurationService feeConfigurationService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<FeeConfigurationResponseDTO> registerFee(@Valid @RequestBody FeeConfigurationRequestDTO dto) {
        FeeConfigurationResponseDTO created = feeConfigurationService.registerFee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{id}")
    public ResponseEntity<FeeConfigurationResponseDTO> getFeeById(@PathVariable Long id) {
        return ResponseEntity.ok(feeConfigurationService.getFeeById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FeeConfigurationResponseDTO>> getAllFeesActiveOnDate(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(feeConfigurationService.getAllFeesActiveOn(effectiveDate, pageable));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{id}")
    public ResponseEntity<FeeConfigurationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FeeConfigurationRequestDTO dto
    ) {
        return ResponseEntity.ok(feeConfigurationService.updateFee(id, dto));
    }
}

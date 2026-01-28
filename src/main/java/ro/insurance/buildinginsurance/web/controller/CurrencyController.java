package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.ICurrencyService;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/currencies")
public class CurrencyController {

    private final ICurrencyService currencyService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<CurrencyResponseDTO> registerCurrency(
            @Valid @RequestBody CurrencyRequestDTO request
    ) {
        CurrencyResponseDTO response = currencyService.registerCurrency(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------  GET  ----------------------

    @GetMapping
    public ResponseEntity<Page<CurrencyResponseDTO>> getAllCurrencies(
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<CurrencyResponseDTO> page = currencyService.getAllCurrencies(pageable);
        return ResponseEntity.ok(page);
    }

    // ----------------------  PUT  ----------------------

    @PostMapping("/{id}/activate")
    public ResponseEntity<CurrencyResponseDTO> activate(@PathVariable Long id) {
        CurrencyResponseDTO response = currencyService.activate(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CurrencyResponseDTO> deactivate(@PathVariable Long id) {
        CurrencyResponseDTO response = currencyService.deactivate(id);
        return ResponseEntity.ok(response);
    }
}

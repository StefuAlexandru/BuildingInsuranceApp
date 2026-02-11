package ro.insurance.buildinginsurance.web.controller.admin.geography;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.country_dto.CountryResponseDTO;
import ro.insurance.buildinginsurance.application.service.ICountryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/geography/countries")
public class CountryController {

    private final ICountryService countryService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<CountryResponseDTO> registerCountry(@Valid @RequestBody CountryRequestDTO countryRequestDTO) {
        CountryResponseDTO countryResponseDTO = countryService.registerCountry(countryRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(countryResponseDTO);
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{countryId}")
    public ResponseEntity<CountryResponseDTO> getCountry(@PathVariable Long countryId) {
        CountryResponseDTO countryResponseDTO = countryService.getCountryById(countryId);
        return ResponseEntity.ok(countryResponseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<CountryResponseDTO>> getCountries(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 5, sort = "name") Pageable pageable
    ) {
        String countryName = (name == null) ? "" : name;
        return ResponseEntity.ok(countryService.getCountryByPartialName(countryName, pageable));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{countryId}")
    public ResponseEntity<CountryResponseDTO> updateCountry(
            @PathVariable Long countryId,
            @Valid @RequestBody CountryRequestDTO countryRequestDTO
    ) {
        CountryResponseDTO countryResponseDTO = countryService.updateCountry(countryId, countryRequestDTO);
        return ResponseEntity.ok(countryResponseDTO);
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{countryId}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long countryId) {
        countryService.deleteCountry(countryId);
        return ResponseEntity.noContent().build();
    }
}

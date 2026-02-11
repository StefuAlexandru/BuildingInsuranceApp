package ro.insurance.buildinginsurance.web.controller.admin.geography;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyResponseDTO;
import ro.insurance.buildinginsurance.application.service.ICountyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/geography/counties")
public class CountyController {

    private final ICountyService countyService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<CountyResponseDTO> registerCounty(@Valid @RequestBody CountyRequestDTO countyRequestDTO) {
        CountyResponseDTO response = countyService.registerCounty(countyRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{countyId}")
    public ResponseEntity<CountyResponseDTO> getCountyById(@PathVariable Long countyId) {
        CountyResponseDTO response = countyService.getCountyById(countyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CountyResponseDTO>> getCounties(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 5, sort = "name") Pageable pageable
    ) {
        String countyName = (name == null) ? "" : name;
        return ResponseEntity.ok(countyService.getCountyByPartialName(countyName, pageable));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{countyId}")
    public ResponseEntity<CountyResponseDTO> updateCounty(
            @PathVariable Long countyId,
            @Valid @RequestBody CountyRequestDTO countyRequestDTO
    ) {
        CountyResponseDTO response = countyService.updateCounty(countyId, countyRequestDTO);
        return ResponseEntity.ok(response);
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{countyId}")
    public ResponseEntity<Void> deleteCounty(@PathVariable Long countyId) {
        countyService.deleteCounty(countyId);
        return ResponseEntity.noContent().build();
    }
}

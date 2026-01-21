package ro.insurance.buildinginsurance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.dto.buildingDto.*;
import ro.insurance.buildinginsurance.service.IBuildingService;

@RestController
@RequestMapping("/api/brokers/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final IBuildingService buildingService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<BuildingResponseDTO> createBuilding(@RequestBody @Valid BuildingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingService.registerNewBuilding(dto));
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{buildingId}")
    public ResponseEntity<BuildingResponseDTO> getBuildingById(@PathVariable Long buildingId) {
        return ResponseEntity.ok(buildingService.getBuildingById(buildingId));
    }

    @GetMapping("/{buildingId}/details")
    public ResponseEntity<BuildingDetailsResponseDTO> getBuildingDetails(@PathVariable Long buildingId) {
        return ResponseEntity.ok(buildingService.getBuildingDetailsById(buildingId));
    }

    @GetMapping
    public ResponseEntity<Page<BuildingResponseDTO>> getBuildingsByClientId(
            @RequestParam Long clientId,
            @PageableDefault(size = 5, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(buildingService.getBuildingsByClientId(clientId, pageable));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{buildingId}")
    public ResponseEntity<BuildingDetailsResponseDTO> updateBuilding(
            @PathVariable Long buildingId,
            @RequestBody @Valid BuildingUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(buildingId, dto));
    }

    // ----------------------  PATCH  ----------------------

    @PatchMapping("/{buildingId}")
    public ResponseEntity<BuildingResponseDTO> patchBuilding(
            @PathVariable Long buildingId,
            @RequestBody @Valid BuildingPatchDTO dto
    ) {
        return ResponseEntity.ok(buildingService.patchBuilding(buildingId, dto));
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long buildingId) {
        buildingService.deleteBuilding(buildingId);
        return ResponseEntity.noContent().build();
    }
}


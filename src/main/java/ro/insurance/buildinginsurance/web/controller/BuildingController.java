package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IBuildingService;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;

@RestController
@RequestMapping("/api/brokers/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final IBuildingService buildingService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<BuildingResponseDTO> createBuilding(
            @RequestBody @Valid BuildingRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildingService.registerNewBuilding(dto, authentication.getName()));
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{buildingId}")
    public ResponseEntity<BuildingResponseDTO> getBuildingById(
            @PathVariable Long buildingId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buildingService.getBuildingById(buildingId, authentication.getName()));
    }

    @GetMapping("/{buildingId}/details")
    public ResponseEntity<BuildingDetailsResponseDTO> getBuildingDetails(
            @PathVariable Long buildingId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buildingService.getBuildingDetailsById(buildingId, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<BuildingResponseDTO>> getBuildingsByClientId(
            @RequestParam Long clientId,
            @PageableDefault(size = 5, sort = "id") Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buildingService.getBuildingsByClientId(clientId, pageable, authentication.getName()));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{buildingId}")
    public ResponseEntity<BuildingDetailsResponseDTO> updateBuilding(
            @PathVariable Long buildingId,
            @RequestBody @Valid BuildingUpdateRequestDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buildingService.updateBuilding(buildingId, dto, authentication.getName()));
    }

    // ----------------------  PATCH  ----------------------

    @PatchMapping("/{buildingId}")
    public ResponseEntity<BuildingResponseDTO> patchBuilding(
            @PathVariable Long buildingId,
            @RequestBody @Valid BuildingPatchDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(buildingService.patchBuilding(buildingId, dto, authentication.getName()));
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<Void> deleteBuilding(
            @PathVariable Long buildingId,
            Authentication authentication
    ) {
        buildingService.deleteBuilding(buildingId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

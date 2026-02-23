package ro.insurance.buildinginsurance.web.controller.broker;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IPolicyService;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.policy_dto.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brokers/policies")
public class PolicyController {

    private final IPolicyService policyService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<PolicyResponseDTO> createPolicy(@Valid @RequestBody PolicyCreateRequestDTO request,Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createDraftPolicy(request,authentication.getName()));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<PolicyResponseDTO> activatePolicy(@PathVariable Long id,Authentication authentication) {
        return ResponseEntity.ok(policyService.activatePolicy(id,authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PolicyResponseDTO> cancelPolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyCancelRequestDTO request,
            Authentication authentication
    ) {
       return ResponseEntity.ok(policyService.cancelPolicy(id, request,authentication.getName()));
    }

    // ----------------------  GET  ----------------------

    @GetMapping
    public ResponseEntity<Page<PolicyListItemDTO>> listPolicies(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication
    ) {
        var sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, sort);

        PolicyListFilterDTO filter = new PolicyListFilterDTO(clientId, null, status, startDate, endDate);
        return ResponseEntity.ok(policyService.listPolicies(filter, pageable, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyDetailsResponseDTO> getPolicyDetails(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(policyService.getPolicyDetails(id,authentication.getName()));
    }

    // ----------------------  PATCH  ----------------------

    @PatchMapping("/{id}")
    public ResponseEntity<PolicyResponseDTO> patchDraftPolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyPatchRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(policyService.patchDraftPolicy(id, request,authentication.getName()));
    }
}

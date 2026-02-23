package ro.insurance.buildinginsurance.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IPolicyService;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyDetailsResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyListFilterDTO;
import ro.insurance.buildinginsurance.web.dto.policy_dto.PolicyListItemDTO;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/policies")
public class AdminPolicyController {

    private final IPolicyService policyService;

    @GetMapping
    public ResponseEntity<Page<PolicyListItemDTO>> listPolicies(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long brokerId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        var sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, sort);
        PolicyListFilterDTO filter = new PolicyListFilterDTO(clientId, brokerId, status, startDate, endDate);

        return ResponseEntity.ok(policyService.listPoliciesForAdmin(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyDetailsResponseDTO> getPolicyDetails(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyDetailsForAdmin(id));
    }
}

package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;

public interface IPolicyService {
    PolicyResponseDTO createDraftPolicy(PolicyCreateRequestDTO policyCreateRequestDTO);
    PolicyResponseDTO activatePolicy(Long id, PolicyActivateRequestDTO request);
    PolicyResponseDTO cancelPolicy(Long id, PolicyCancelRequestDTO request);

    Page<PolicyListItemDTO> listPolicies(PolicyListFilterDTO filter, Pageable pageable);
    PolicyResponseDTO getPolicyDetails(Long id);
}

package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.policy_dto.*;

public interface IPolicyService {
    PolicyResponseDTO createDraftPolicy(PolicyCreateRequestDTO policyCreateRequestDTO,String brokerEmail);
    PolicyResponseDTO activatePolicy(Long id,String brokerName);
    PolicyResponseDTO cancelPolicy(Long id, PolicyCancelRequestDTO request,String brokerEmail);

    Page<PolicyListItemDTO> listPolicies(PolicyListFilterDTO filter, Pageable pageable,String brokerEmail);
    PolicyDetailsResponseDTO getPolicyDetails(Long id,String brokerEmail);

    PolicyResponseDTO patchDraftPolicy(Long id,PolicyPatchRequestDTO policyPatchRequestDTO,String brokerEmail);
}
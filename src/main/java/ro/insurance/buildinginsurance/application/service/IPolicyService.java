package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;

public interface IPolicyService {
    PolicyResponseDTO createDraftPolicy(PolicyCreateRequestDTO policyCreateRequestDTO);
    PolicyResponseDTO activatePolicy(Long id,String brokerName);
    PolicyResponseDTO cancelPolicy(Long id, PolicyCancelRequestDTO request,String brokerName);

    Page<PolicyListItemDTO> listPolicies(PolicyListFilterDTO filter, Pageable pageable,String brokerName);
    PolicyDetailsResponseDTO getPolicyDetails(Long id,String brokerName);

    PolicyResponseDTO patchDraftPolicy(Long id,PolicyPatchRequestDTO policyPatchRequestDTO,String brokerName);
}
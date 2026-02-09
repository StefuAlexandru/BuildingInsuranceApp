package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.web.dto.policy_calculation_dto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policy_calculation_dto.PolicyCalculationResponseDTO;

public interface IPolicyCalculationService {
    PolicyCalculationResponseDTO calculatePremium(PolicyCalculationRequestDTO policyCalculationRequest);
}

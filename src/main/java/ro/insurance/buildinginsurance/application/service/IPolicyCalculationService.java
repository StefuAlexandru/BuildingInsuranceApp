package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;

public interface IPolicyCalculationService {
    PolicyCalculationResponseDTO calculatePremium(PolicyCalculationRequestDTO policyCalculationRequest);
}

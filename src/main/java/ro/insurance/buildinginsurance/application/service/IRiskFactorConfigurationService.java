package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface IRiskFactorConfigurationService {
    RiskFactorConfigurationResponseDTO createRiskFactorConfiguration(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO);

    Page<RiskFactorConfigurationResponseDTO> getAllRiskFactors(Pageable pageable);
    List<RiskFactorConfigurationResponseDTO> getRiskFactorsForBuilding(Long buildingId, LocalDate effectiveDate);

    RiskFactorConfigurationResponseDTO updateRiskFactorConfiguration(Long id,RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO);
}

package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationResponseDTO;

import java.time.LocalDate;

public interface IFeeConfigurationService {
    FeeConfigurationResponseDTO registerFee(FeeConfigurationRequestDTO feeConfigurationRequestDTO);

    FeeConfigurationResponseDTO getFeeById(Long id);

    Page<FeeConfigurationResponseDTO> getAllFeesActiveOn(LocalDate date, Pageable pageable);

    FeeConfigurationResponseDTO updateFee(Long id, FeeConfigurationRequestDTO feeConfigurationRequestDTO);
}

package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.facade.RiskFactorConfigurationFacade;
import ro.insurance.buildinginsurance.application.mapper.RiskFactorConfigurationMapper;
import ro.insurance.buildinginsurance.application.service.IRiskFactorConfigurationService;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.risk_factor_configuration_dto.RiskFactorConfigurationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RiskFactorConfigurationService implements IRiskFactorConfigurationService {

    private final RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    private final RiskFactorConfigurationMapper riskFactorConfigurationMapper;
    private final RiskFactorConfigurationFacade riskFactorConfigurationFacade;


    // --------------------- CREATE ---------------------

    @Override
    public RiskFactorConfigurationResponseDTO createRiskFactorConfiguration(RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        log.info("Create risk factor configuration request: level={}, referenceId={}, buildingType={}, effective_from={}",
                riskFactorConfigurationRequestDTO.level(),
                riskFactorConfigurationRequestDTO.referenceId(),
                riskFactorConfigurationRequestDTO.buildingType(),
                riskFactorConfigurationRequestDTO.effectiveFrom());

        riskFactorConfigurationFacade.validateCreateOrThrow(riskFactorConfigurationRequestDTO);

        RiskFactorConfiguration entity = riskFactorConfigurationMapper.toEntity(riskFactorConfigurationRequestDTO);
        RiskFactorConfiguration saved = riskFactorConfigurationRepository.save(entity);

        return riskFactorConfigurationMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public Page<RiskFactorConfigurationResponseDTO> getAllRiskFactors(Pageable pageable) {
        log.info("Get all risk factor configurations request: page={}", pageable);
        return riskFactorConfigurationRepository.findAll(pageable)
                .map(riskFactorConfigurationMapper::toResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskFactorConfigurationResponseDTO> getRiskFactorsForBuilding(Long buildingId, LocalDate effectiveDate) {
        log.info("Get all active factors for buildingId={}, date={}", buildingId, effectiveDate);
        return riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(buildingId,effectiveDate)
                .stream().map(riskFactorConfigurationMapper::toResponse).toList();
    }

    // --------------------- UPDATE ---------------------

    @Override
    public RiskFactorConfigurationResponseDTO updateRiskFactorConfiguration(Long id,RiskFactorConfigurationRequestDTO riskFactorConfigurationRequestDTO) {
        log.info("Update risk factor configuration request on id ={} : level={}, referenceId={}, buildingType={}, effective_from={}",
                id,
                riskFactorConfigurationRequestDTO.level(),
                riskFactorConfigurationRequestDTO.referenceId(),
                riskFactorConfigurationRequestDTO.buildingType(),
                riskFactorConfigurationRequestDTO.effectiveFrom());

        RiskFactorConfiguration entity = findRiskFactorConfigurationOrThrow(id);

        riskFactorConfigurationFacade.validateUpdateOrThrow(id,riskFactorConfigurationRequestDTO);

        riskFactorConfigurationMapper.updateFromDTO(riskFactorConfigurationRequestDTO,entity);
        RiskFactorConfiguration saved = riskFactorConfigurationRepository.save(entity);

        return riskFactorConfigurationMapper.toResponse(saved);
    }

    // --------------------- HELPERS ---------------------

    private RiskFactorConfiguration findRiskFactorConfigurationOrThrow(Long id) {
        return riskFactorConfigurationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Find risk factor configuration with id ={}, but not found", id);
                    return new BusinessException(ErrorCode.RISK_FACTOR_CONFIGURATION_NOT_FOUND);
                });
    }


}

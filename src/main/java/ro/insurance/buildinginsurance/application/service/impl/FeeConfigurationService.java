package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.mapper.FeeConfigurationMapper;
import ro.insurance.buildinginsurance.application.service.IFeeConfigurationService;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.fee_configuration_dto.FeeConfigurationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeeConfigurationService implements IFeeConfigurationService {

    private final FeeConfigurationMapper feeConfigurationMapper;
    private final FeeConfigurationRepository feeConfigurationRepository;

    // --------------------- CREATE ---------------------

    @Override
    public FeeConfigurationResponseDTO registerFee(FeeConfigurationRequestDTO feeConfigurationRequestDTO) {
        log.info("Register fee configuration request, name={}",feeConfigurationRequestDTO.name().trim());
        validateCreateOrThrow(feeConfigurationRequestDTO);

        FeeConfiguration entity = feeConfigurationMapper.toEntity(feeConfigurationRequestDTO);
        FeeConfiguration saved = feeConfigurationRepository.save(entity);

        return feeConfigurationMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public FeeConfigurationResponseDTO getFeeById(Long id) {
        log.info("Get fee configuration by id={}", id);
        FeeConfiguration entity = findFeeConfigurationOrThrow(id);
        return  feeConfigurationMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeeConfigurationResponseDTO> getAllFeesActiveOn(LocalDate date, Pageable pageable) {
        log.info("Get all fees active on date={}, pageable={}", date, pageable);
        return feeConfigurationRepository.getAllFeesActiveOn(date,pageable)
                .map(feeConfigurationMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public FeeConfigurationResponseDTO updateFee(Long id, FeeConfigurationRequestDTO feeConfigurationRequestDTO) {
        log.info("Update fee configuration request by id={}", id);
        FeeConfiguration entity = findFeeConfigurationOrThrow(id);

        validateFeeUpdateOrThrow(id,feeConfigurationRequestDTO);
        feeConfigurationMapper.updateFromDTO(feeConfigurationRequestDTO,entity);

        FeeConfiguration saved = feeConfigurationRepository.save(entity);
        return feeConfigurationMapper.toResponse(saved);
    }

    // --------------------- HELPERS ---------------------

    private void validateDatesOrThrow(FeeConfigurationRequestDTO feeConfigurationRequestDTO) {
        LocalDate effectiveFrom = feeConfigurationRequestDTO.effectiveFrom();
        LocalDate effectiveTo = feeConfigurationRequestDTO.effectiveTo();

        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            log.warn("Invalid dates, effectiveFrom={}, effectiveTo={}", effectiveFrom, effectiveTo);
            throw new BusinessException(ErrorCode.INVALID_DATES);
        }

    }

    private void validateCreateOrThrow(FeeConfigurationRequestDTO feeConfigurationRequestDTO) {
        validateDatesOrThrow(feeConfigurationRequestDTO);

        if(feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(
                feeConfigurationRequestDTO.name().trim(),
                feeConfigurationRequestDTO.feeType().name(),
                feeConfigurationRequestDTO.effectiveFrom())) {
            log.warn("Fee configuration with name {} and type {} already exists", feeConfigurationRequestDTO.name().trim(),feeConfigurationRequestDTO.feeType());
            throw new BusinessException(ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS);
        }

        if (feeConfigurationRepository.existsOverlappingIntervalForCreate(
                feeConfigurationRequestDTO.feeType().name(),
                feeConfigurationRequestDTO.name().trim(),
                feeConfigurationRequestDTO.effectiveFrom(),
                feeConfigurationRequestDTO.effectiveTo())) {

            log.warn("Fee configuration overlap: name={}, type={}, from={}, to={}",
                    feeConfigurationRequestDTO.name().trim(),
                    feeConfigurationRequestDTO.feeType(),
                    feeConfigurationRequestDTO.effectiveFrom(),
                    feeConfigurationRequestDTO.effectiveTo());

            throw new BusinessException(ErrorCode.FEE_CONFIGURATION_OVERLAP);
        }
    }

    private void validateFeeUpdateOrThrow(Long id, FeeConfigurationRequestDTO feeConfigurationRequestDTO) {
        validateDatesOrThrow(feeConfigurationRequestDTO);

        if (feeConfigurationRepository.existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(
                feeConfigurationRequestDTO.name().trim(),
                feeConfigurationRequestDTO.feeType().name(),
                feeConfigurationRequestDTO.effectiveFrom(),
                id)) {
            log.warn("Fee configuration already exists: name={}, type={}, effectiveFrom={}, excludingId={}",
                    feeConfigurationRequestDTO.name().trim(),
                    feeConfigurationRequestDTO.feeType(),
                    feeConfigurationRequestDTO.effectiveFrom(),
                    id);
            throw new BusinessException(ErrorCode.FEE_CONFIGURATION_ALREADY_EXISTS);
        }

        if (feeConfigurationRepository.existsOverlappingIntervalForUpdate(
                id,
                feeConfigurationRequestDTO.feeType().name(),
                feeConfigurationRequestDTO.name().trim(),
                feeConfigurationRequestDTO.effectiveFrom(),
                feeConfigurationRequestDTO.effectiveTo())) {

            log.warn("Fee configuration overlap on update: id={}, name={}, type={}, from={}, to={}",
                    id,
                    feeConfigurationRequestDTO.name().trim(),
                    feeConfigurationRequestDTO.feeType(),
                    feeConfigurationRequestDTO.effectiveFrom(),
                    feeConfigurationRequestDTO.effectiveTo());

            throw new BusinessException(ErrorCode.FEE_CONFIGURATION_OVERLAP);
        }
    }

    private FeeConfiguration findFeeConfigurationOrThrow(Long id){
        return feeConfigurationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fee configuration not found by id={}", id);
                    return new BusinessException(ErrorCode.FEE_CONFIGURATION_NOT_FOUND);
                });
    }


}

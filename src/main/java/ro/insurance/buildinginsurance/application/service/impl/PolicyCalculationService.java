package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.IPolicyCalculationService;
import ro.insurance.buildinginsurance.domain.entity.Building;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BuildingRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.FeeConfigurationRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RiskFactorConfigurationRepository;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.AppliedAdjustment;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyCalculationService implements IPolicyCalculationService {

    private final BuildingRepository buildingRepository;
    private final FeeConfigurationRepository feeConfigurationRepository;
    private final RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    private final CurrencyRepository currencyRepository;


    @Override
    public PolicyCalculationResponseDTO calculatePremium(PolicyCalculationRequestDTO policyCalculationRequestDTO) {
        Long buildingId = policyCalculationRequestDTO.buildingId();
        Long currencyId = policyCalculationRequestDTO.currencyId();

        LocalDate date = policyCalculationRequestDTO.now() == null? LocalDate.now() : policyCalculationRequestDTO.now();

        Building building = validateBuildingOrThrow(buildingId);
        validateCurrencyOrThrow(currencyId);

        List<FeeConfiguration> activeFees = feeConfigurationRepository.findAllActiveOn(date);
        List<RiskFactorConfiguration> riskFees = riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(building.getId(), date);

        BigDecimal feesPercent = activeFees.stream()
                .map(FeeConfiguration::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal riskPercent = riskFees.stream()
                .map(RiskFactorConfiguration::getAdjustmentPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPercent = feesPercent.add(riskPercent);

        List<AppliedAdjustment> breakdown = Stream.concat(
                activeFees.stream()
                        .map(f -> new AppliedAdjustment("FEE",f.getFeeType().name(),f.getPercentage())),
                riskFees.stream()
                        .map(r -> new AppliedAdjustment("RISK",riskCode(r),r.getAdjustmentPercentage()))
        ).toList();

        BigDecimal multiplier = BigDecimal.ONE.add(totalPercent);
        BigDecimal finalPremium = policyCalculationRequestDTO.basePremium().multiply(multiplier);

        finalPremium = finalPremium.setScale(2, RoundingMode.HALF_UP);

        return new PolicyCalculationResponseDTO(finalPremium,totalPercent,breakdown);
    }

    // --------------------- HELPERS ---------------------

    private Building validateBuildingOrThrow(Long buildingId){
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> {
                    log.warn("Building with id {} not found for premium calculation", buildingId);
                    return new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
                });
    }

    private void validateCurrencyOrThrow(Long currencyId){
        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> {
                    log.warn("Currency with id {} not found for premium calculation", currencyId);
                    return new BusinessException(ErrorCode.CURRENCY_NOT_FOUND);
                });

        if(!currency.isActive()){
            log.warn("Currency with id {} is not active for premium calculation", currencyId);
            throw new BusinessException(ErrorCode.CURRENCY_IS_NOT_ACTIVE);
        }
    }

    private static String riskCode(RiskFactorConfiguration riskFactorConfiguration){
        return switch(riskFactorConfiguration.getLevel()){
            case CITY,COUNTY,COUNTRY -> riskFactorConfiguration.getLevel().name() + ":" + riskFactorConfiguration.getReferenceId();
            case BUILDING_TYPE -> riskFactorConfiguration.getLevel().name() + ":" + riskFactorConfiguration.getBuildingType().name();
        };
    }
}

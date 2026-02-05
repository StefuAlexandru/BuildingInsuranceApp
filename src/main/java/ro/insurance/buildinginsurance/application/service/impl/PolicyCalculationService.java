package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.service.IPolicyCalculationService;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
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
import java.util.List;
import java.util.stream.Stream;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyCalculationService implements IPolicyCalculationService {

    private final FeeConfigurationRepository feeConfigurationRepository;
    private final RiskFactorConfigurationRepository riskFactorConfigurationRepository;
    private final CurrencyRepository currencyRepository;


    @Override
    public PolicyCalculationResponseDTO calculatePremium(PolicyCalculationRequestDTO policyCalculationRequestDTO) {
        LocalDate date = policyCalculationRequestDTO.now() == null? LocalDate.now() : policyCalculationRequestDTO.now();

        List<FeeConfiguration> activeFees = feeConfigurationRepository.findAllActiveOn(date);
        List<RiskFactorConfiguration> riskFees = riskFactorConfigurationRepository.findRiskFactorsByBuildingIdActiveOn(policyCalculationRequestDTO.buildingId(), date);

        BigDecimal feesPercent = activeFees.stream()
                .map(FeeConfiguration::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal riskPercent = riskFees.stream()
                .map(RiskFactorConfiguration::getAdjustmentPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPercentRaw = feesPercent.add(riskPercent);
        BigDecimal totalPercent = totalPercentRaw.divide(new BigDecimal("100"),6,RoundingMode.HALF_UP);

        List<AppliedAdjustment> breakdown = Stream.concat(
                activeFees.stream()
                        .map(f -> new AppliedAdjustment("FEE",f.getFeeType().name(),f.getPercentage())),
                riskFees.stream()
                        .map(r -> new AppliedAdjustment("RISK",riskCode(r),r.getAdjustmentPercentage()))
        ).toList();

        BigDecimal multiplier = BigDecimal.ONE.add(totalPercent);
        BigDecimal finalPremium = policyCalculationRequestDTO.basePremium().multiply(multiplier);
        if(finalPremium.compareTo(BigDecimal.ZERO)<=0){
            log.warn("Policy final premium is negative");
            throw new BusinessException(ErrorCode.INVALID_FINAL_PREMIUM);
        }

        Currency currency = currencyRepository.findById(policyCalculationRequestDTO.currencyId())
                .orElseThrow(() -> {
                    log.warn("Currency with id {} not found for policy calculation", policyCalculationRequestDTO.currencyId());
                    return new BusinessException(ErrorCode.CURRENCY_NOT_FOUND);}
                );

        finalPremium = convertFromBase(finalPremium,currency);

        finalPremium = finalPremium.setScale(2, RoundingMode.HALF_UP);

        return new PolicyCalculationResponseDTO(finalPremium,totalPercentRaw,breakdown);
    }

    // --------------------- HELPERS ---------------------

    private static String riskCode(RiskFactorConfiguration riskFactorConfiguration){
        return switch(riskFactorConfiguration.getLevel()){
            case CITY,COUNTY,COUNTRY -> riskFactorConfiguration.getLevel().name() + " : " + riskFactorConfiguration.getReferenceId();
            case BUILDING_TYPE -> riskFactorConfiguration.getLevel().name() + " : " + riskFactorConfiguration.getBuildingType().name();
        };
    }

    private BigDecimal convertFromBase(BigDecimal amountRon, Currency currency) {
        if ("RON".equalsIgnoreCase(currency.getCode())) return amountRon;

        BigDecimal rate = currency.getExchangeRateToBase();
        return amountRon.divide(rate, 6, RoundingMode.HALF_UP);
    }
}

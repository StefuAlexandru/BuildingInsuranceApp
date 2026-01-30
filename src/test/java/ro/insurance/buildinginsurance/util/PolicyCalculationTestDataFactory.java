package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyCalculationTestDataFactory {

    // -------------------  DTOS  -------------------

    public static PolicyCalculationRequestDTO aPolicyCalculationRequestWithBuildingId(Long buildingId) {
        LocalDate date = LocalDate.of(2026, 1, 30);
        return new PolicyCalculationRequestDTO(new BigDecimal("100.00"), buildingId,1L,date);
    }
    public static PolicyCalculationRequestDTO aPolicyCalculationRequestWithDate(LocalDate date) {
        return new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L,1L,date);
    }

    public static PolicyCalculationRequestDTO aPolicyCalculationRequestWithCurrencyId(Long currencyId) {
        LocalDate date = LocalDate.of(2026, 1, 30);
        return new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L,currencyId,date);
    }
    public static PolicyCalculationRequestDTO aPolicyCalculationRequest() {
        LocalDate date = LocalDate.of(2026, 1, 30);
        return new PolicyCalculationRequestDTO(new BigDecimal("100.00"), 1L,1L,date);
    }


}

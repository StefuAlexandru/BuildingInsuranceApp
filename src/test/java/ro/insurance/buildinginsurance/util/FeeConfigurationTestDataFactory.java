package ro.insurance.buildinginsurance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.feeConfigurationDto.FeeConfigurationResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class FeeConfigurationTestDataFactory {

    private FeeConfigurationTestDataFactory() {}

    // -------------------  BUILDERS  -------------------

    public static String createFeeRequestBody(ObjectMapper objectMapper, String name, FeeType feeType, BigDecimal percentage, LocalDate effectiveFrom, LocalDate effectiveTo) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("feeType", feeType);
        body.put("percentage", percentage);
        body.put("effectiveFrom", effectiveFrom.toString());
        body.put("effectiveTo", effectiveTo != null ? effectiveTo.toString() : null);
        return objectMapper.writeValueAsString(body);
    }

    // -------------------  DTOS  -------------------

    public static FeeConfigurationRequestDTO aFeeRequest() {
        return new FeeConfigurationRequestDTO(
                "Standard admin fee", FeeType.ADMIN_FEE, new BigDecimal("5.00"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    public static FeeConfigurationRequestDTO aFeeRequestWithName(String name) {
        FeeConfigurationRequestDTO base = aFeeRequest();
        return new FeeConfigurationRequestDTO(name, base.feeType(), base.percentage(), base.effectiveFrom(), base.effectiveTo());
    }

    public static FeeConfigurationRequestDTO aFeeRequestWithDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        FeeConfigurationRequestDTO base = aFeeRequest();
        return new FeeConfigurationRequestDTO(base.name(), base.feeType(), base.percentage(),
                effectiveFrom, effectiveTo);
    }

    public static FeeConfigurationResponseDTO aFeeResponse() {
        FeeConfigurationRequestDTO base = aFeeRequest();
        return new FeeConfigurationResponseDTO(1L, base.name(), base.feeType(), base.percentage(),
                base.effectiveFrom(), base.effectiveTo(), true);
    }

    // -------------------  ENTITY  -------------------

    public static FeeConfiguration aFee() {
        return aFeeWithId(1L);
    }

    public static FeeConfiguration aFeeWithId(Long id) {
        FeeConfiguration fee = new FeeConfiguration();
        fee.setId(id);
        fee.setName("Standard admin fee");
        fee.setFeeType(FeeType.ADMIN_FEE);
        fee.setPercentage(new BigDecimal("5.00"));
        fee.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        fee.setEffectiveTo(LocalDate.of(2026, 12, 31));
        fee.setActive(true);
        return fee;
    }


    public static FeeConfiguration aFeeWithoutId() {
        FeeConfiguration fee = new FeeConfiguration();
        fee.setName("Standard admin fee");
        fee.setFeeType(FeeType.ADMIN_FEE);
        fee.setPercentage(new BigDecimal("5.00"));
        fee.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        fee.setEffectiveTo(LocalDate.of(2026, 12, 31));
        fee.setActive(true);
        return fee;
    }
}

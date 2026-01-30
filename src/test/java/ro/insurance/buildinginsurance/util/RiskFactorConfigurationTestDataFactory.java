package ro.insurance.buildinginsurance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationRequestDTO;
import ro.insurance.buildinginsurance.web.dto.riskFactorConfigurationDto.RiskFactorConfigurationResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class RiskFactorConfigurationTestDataFactory {

    private RiskFactorConfigurationTestDataFactory() {}

    // -------------------  BUILDERS  -------------------

    public static String createRiskFactorRequestBody(
            ObjectMapper objectMapper,
            RiskFactorLevel level,
            Long referenceId,
            BuildingType buildingType,
            BigDecimal adjustmentPercentage,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("level", level);
        body.put("referenceId", referenceId);
        body.put("buildingType", buildingType);
        body.put("adjustmentPercentage", adjustmentPercentage);
        body.put("effectiveFrom", effectiveFrom.toString());
        body.put("effectiveTo", effectiveTo != null ? effectiveTo.toString() : null);
        return objectMapper.writeValueAsString(body);
    }


    // -------------------  DTOS  -------------------

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestCity() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.CITY,
                10L,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationResponseDTO aRiskFactorConfigurationResponse() {
        return new RiskFactorConfigurationResponseDTO(
                1L,
                RiskFactorLevel.CITY,
                10L,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestInvalidBothNull() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.CITY,
                null,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestInvalidBothSet() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.CITY,
                10L,
                BuildingType.RESIDENTIAL,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestInvalidDates() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.CITY,
                10L,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestInvalidBuildingTypeLevelWithReferenceId() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.BUILDING_TYPE,
                10L,
                null,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestInvalidCityLevelWithBuildingType() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.CITY,
                null,
                BuildingType.RESIDENTIAL,
                new BigDecimal("0.1000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestCounty() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.COUNTY,
                20L,
                null,
                new BigDecimal("0.0800"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestCountry() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.COUNTRY,
                30L,
                null,
                new BigDecimal("0.0500"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static RiskFactorConfigurationRequestDTO aRiskFactorConfigurationRequestBuildingType() {
        return new RiskFactorConfigurationRequestDTO(
                RiskFactorLevel.BUILDING_TYPE,
                null,
                BuildingType.RESIDENTIAL,
                new BigDecimal("0.0500"),
                LocalDate.of(2026, 1, 1),
                null
        );
    }


    // -------------------  ENTITY  -------------------

    public static RiskFactorConfiguration aRisk(RiskFactorLevel level, Long refId,
                                                BuildingType buildingType, BigDecimal pct) {
        RiskFactorConfiguration r = new RiskFactorConfiguration();
        r.setLevel(level);
        r.setReferenceId(refId);
        r.setBuildingType(buildingType);
        r.setAdjustmentPercentage(pct);
        return r;
    }

    public static RiskFactorConfiguration aRiskFactorConfiguration() {
        RiskFactorConfiguration e = new RiskFactorConfiguration();
        e.setLevel(RiskFactorLevel.CITY);
        e.setReferenceId(10L);
        e.setBuildingType(null);
        e.setAdjustmentPercentage(new BigDecimal("0.1000"));
        e.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        e.setEffectiveTo(LocalDate.of(2026, 12, 31));
        return e;
    }

    public static RiskFactorConfiguration aRiskFactorConfigurationWithId(Long id) {
        RiskFactorConfiguration e = aRiskFactorConfiguration();
        e.setId(id);
        return e;
    }
}

package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.entity.Policy;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyCreateRequestDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.PolicyResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientWithId;
import static ro.insurance.buildinginsurance.util.CurrencyTestDataFactory.aCurrencyWithId;

public class PolicyTestDataFactory {

    // -------------------  DTOS  -------------------
    public static PolicyCreateRequestDTO aPolicyCreateRequest() {
        return new PolicyCreateRequestDTO(
                1L, 1L, 1L, 1L,
                LocalDate.of(2026, 1, 30),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00")
        );
    }

    public static PolicyCreateRequestDTO aPolicyCreateRequest(Long clientId, Long buildingId, Long brokerId, Long currencyId, LocalDate startDate, LocalDate endDate) {
        return new PolicyCreateRequestDTO(
                clientId, buildingId, brokerId, currencyId,
                startDate, endDate,
                new BigDecimal("100.00")
        );
    }

    public static PolicyResponseDTO aPolicyResponse() {
        return new PolicyResponseDTO(
                10L,
                "POL-20260202-ABCDEF",
                PolicyStatus.DRAFT,
                1L, 1L, 1L,
                1L, "RON",
                LocalDate.of(2026, 1, 30),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"),
                new PolicyCalculationResponseDTO(
                        new BigDecimal("130.00"),
                        new BigDecimal("30"),
                        List.of()
                ),
                new BigDecimal("10.00"),
                new BigDecimal("13.00"),
                null, null, null
        );
    }

    // ------------------- ENTITIES -------------------

    public static Policy aPolicyEntityDraft() {
        Policy p = new Policy();
        p.setStatus(PolicyStatus.DRAFT);
        p.setBasePremiumAmount(new BigDecimal("100.00"));
        return p;
    }

    public static Policy aDraftPolicyWithAllMandatoryFields(Long id) {
        Policy p = new Policy();
        p.setId(id);
        p.setStatus(PolicyStatus.DRAFT);

        p.setClient(aClientWithId(1L));
        p.setBuilding(aBuildingWithId(1L));
        p.setBroker(aBrokerWithId(1L));

        Currency currency = aCurrencyWithId(1L);
        currency.setCode("RON");
        p.setCurrency(currency);

        p.setStartDate(LocalDate.now());
        p.setEndDate(LocalDate.now().plusDays(10));
        p.setBasePremiumAmount(new BigDecimal("100.00"));
        p.getBroker().setCommissionPct(new BigDecimal("10.00"));
        p.setFinalPremiumAmount(new BigDecimal("0.00"));
        return p;
    }
}

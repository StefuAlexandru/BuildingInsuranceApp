package ro.insurance.buildinginsurance.application.mapper;

import org.mapstruct.*;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.web.dto.policyCalculationDto.PolicyCalculationResponseDTO;
import ro.insurance.buildinginsurance.web.dto.policyDto.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "policyNumber", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "finalPremiumAmount", ignore = true)
    @Mapping(target = "activatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "brokerCommissionPct", ignore = true)
    @Mapping(target = "brokerCommissionAmount", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "broker", source = "broker")
    @Mapping(target = "currency", source = "currency")
    Policy toEntity(PolicyCreateRequestDTO request, Client client, Building building, Broker broker, Currency currency);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "building", ignore = true)
    @Mapping(target = "broker", source = "broker")
    @Mapping(target = "currency", source = "currency")
    void updateFromDTO(PolicyPatchRequestDTO request, Broker broker, Currency currency, @MappingTarget Policy policy);

    @Mapping(target = "clientId", source = "policy.client.id")
    @Mapping(target = "buildingId", source = "policy.building.id")
    @Mapping(target = "brokerId", source = "policy.broker.id")
    @Mapping(target = "currencyId", source = "policy.currency.id")
    @Mapping(target = "currencyCode", source = "policy.currency.code")
    @Mapping(target = "calculationResponse", source = "calculationResponse")
    @Mapping(target = "brokerCommissionPct", source = "policy.brokerCommissionPct")
    @Mapping(target = "brokerCommissionAmount", source = "policy.brokerCommissionAmount")
    PolicyResponseDTO toResponse(Policy policy, PolicyCalculationResponseDTO calculationResponse);


    @Mapping(target = "status", constant = "ACTIVE")
    void applyActivation(PolicyActivateRequestDTO request, @MappingTarget Policy policy);
    @Mapping(target = "status", constant = "CANCELLED")
    @Mapping(target = "cancelledAt", source = "cancelledAt")
    void applyCancel(PolicyCancelRequestDTO request, OffsetDateTime cancelledAt, @MappingTarget Policy policy);

    @Mapping(target = "premium", expression = "java(resolvePremium(policy))")
    @Mapping(target = "currencyCode", source = "currency.code")
    @Mapping(target = "clientName", expression = "java(resolveClientName(policy))")
    @Mapping(target = "buildingAddress", expression = "java(resolveBuildingAddress(policy))")
    PolicyListItemDTO toListItem(Policy policy);

    default BigDecimal resolvePremium(Policy policy) {
        if (policy.getFinalPremiumAmount() != null) return policy.getFinalPremiumAmount();
        return policy.getBasePremiumAmount();
    }

    default String resolveClientName(Policy policy) {
        return policy.getClient() == null ? null : policy.getClient().getName();
    }

    default String resolveBuildingAddress(Policy policy) {
        if (policy.getBuilding() == null) return null;

        String street = policy.getBuilding().getStreet();
        String number = policy.getBuilding().getNumber();

        String base = (street != null ? street : "") + (number != null ? " " + number : "");

        return base.trim();
    }

}


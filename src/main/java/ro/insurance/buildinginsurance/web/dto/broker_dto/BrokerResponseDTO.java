package ro.insurance.buildinginsurance.web.dto.broker_dto;

import java.math.BigDecimal;

public record BrokerResponseDTO(
        Long id,
        String brokerCode,
        String name,
        String email,
        String phone,
        BigDecimal commissionPct
) {}

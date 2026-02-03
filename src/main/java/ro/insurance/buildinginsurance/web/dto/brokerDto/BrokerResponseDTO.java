package ro.insurance.buildinginsurance.web.dto.brokerDto;

import java.math.BigDecimal;

public record BrokerResponseDTO(
        Long id,
        String brokerCode,
        String name,
        String email,
        String phone,
        BigDecimal commissionPct
) {}

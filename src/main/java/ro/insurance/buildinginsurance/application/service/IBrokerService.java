package ro.insurance.buildinginsurance.application.service;

import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerResponseDTO;

public interface IBrokerService {
    BrokerResponseDTO createBroker(BrokerRequestDTO brokerRequestDTO);

    BrokerResponseDTO activateBroker(Long brokerId);
    BrokerResponseDTO deactivateBroker(Long brokerId);
}

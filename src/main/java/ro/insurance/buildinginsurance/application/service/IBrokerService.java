package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerResponseDTO;

public interface IBrokerService {
    BrokerResponseDTO createBroker(BrokerRequestDTO brokerRequestDTO);

    BrokerResponseDTO activateBroker(Long brokerId);
    BrokerResponseDTO deactivateBroker(Long brokerId);

    BrokerResponseDTO getBroker(Long id);
    Page<BrokerResponseDTO> getBrokers(Pageable pageable);

    BrokerResponseDTO updateBroker(Long id, BrokerRequestDTO brokerRequestDTO);
}

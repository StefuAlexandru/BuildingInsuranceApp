package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.mapper.BrokerMapper;
import ro.insurance.buildinginsurance.application.service.IBrokerService;
import ro.insurance.buildinginsurance.domain.entity.Broker;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BrokerRepository;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BrokerService implements IBrokerService {

    private final BrokerRepository brokerRepository;
    private final BrokerMapper brokerMapper;

    @Override
    public BrokerResponseDTO createBroker(BrokerRequestDTO brokerRequestDTO) {
        log.warn("Creating new broker request");

        if (brokerRepository.existsByBrokerCodeIgnoreCase(brokerRequestDTO.brokerCode())) {
            log.warn("Broker code {} already exists", brokerRequestDTO.brokerCode());
            throw new BusinessException(ErrorCode.BROKER_CODE_ALREADY_EXISTS);
        }

        Broker broker = brokerMapper.toEntity(brokerRequestDTO);
        Broker saved = brokerRepository.save(broker);
        return brokerMapper.toResponse(saved);
    }
}

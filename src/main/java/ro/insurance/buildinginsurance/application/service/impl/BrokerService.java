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

    // ---------------------  CREATE  ---------------------

    @Override
    public BrokerResponseDTO createBroker(BrokerRequestDTO brokerRequestDTO) {
        log.info("Creating new broker request");

        if (brokerRepository.existsByBrokerCodeIgnoreCase(brokerRequestDTO.brokerCode())) {
            log.warn("Broker code {} already exists", brokerRequestDTO.brokerCode());
            throw new BusinessException(ErrorCode.BROKER_CODE_ALREADY_EXISTS);
        }

        Broker broker = brokerMapper.toEntity(brokerRequestDTO);
        Broker saved = brokerRepository.save(broker);
        return brokerMapper.toResponse(saved);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public BrokerResponseDTO activateBroker(Long brokerId) {
        log.info("Activating broker. brokerId={}", brokerId);
        Broker broker = findBrokerOrThrow(brokerId);

        if(broker.isActive()) {
            log.info("Broker already active. brokerId={}", brokerId);
            return brokerMapper.toResponse(broker);
        }

        broker.setActive(true);
        Broker saved = brokerRepository.save(broker);
        log.info("Broker activated. brokerId={}", brokerId);
        return brokerMapper.toResponse(saved);
    }

    @Override
    public BrokerResponseDTO deactivateBroker(Long brokerId) {
        log.info("Deactivating broker. brokerId={}", brokerId);
        Broker broker = findBrokerOrThrow(brokerId);

        if(!broker.isActive()) {
            log.info("Broker already inactive. brokerId={}", brokerId);
            return brokerMapper.toResponse(broker);
        }

        broker.setActive(false);
        Broker saved = brokerRepository.save(broker);
        log.info("Broker deactivated. brokerId={}", brokerId);
        return brokerMapper.toResponse(saved);
    }

    // ---------------------  HELPERS  ---------------------

    private Broker findBrokerOrThrow(Long id){
        return brokerRepository.findById(id).orElseThrow(() -> {
                    log.warn("Broker with id {} not found", id);
                    return new BusinessException(ErrorCode.BROKER_NOT_FOUND);
                });
    }

}

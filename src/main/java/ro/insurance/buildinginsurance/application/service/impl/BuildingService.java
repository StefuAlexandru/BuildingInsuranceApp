package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.mapper.BuildingMapper;
import ro.insurance.buildinginsurance.application.security.CurrentBrokerProvider;
import ro.insurance.buildinginsurance.application.service.IBuildingService;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BuildingService implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final ClientRepository clientRepository;
    private final CityRepository cityRepository;
    private final BuildingMapper buildingMapper;
    private final CurrentBrokerProvider currentBrokerProvider;

    // ---------------------  CREATE  ---------------------

    @Override
    public BuildingResponseDTO registerNewBuilding(BuildingRequestDTO dto, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);

        log.info("Register building: brokerId={}, clientId={}, cityId={}",
                broker.getId(), dto.clientId(), dto.cityId());

        Client client = findClientOrThrow(dto.clientId());
        City city = findCityOrThrow(dto.cityId());

        Building building = buildingMapper.toEntity(dto, client, city);
        Building saved = buildingRepository.save(building);

        log.info("Building registered: id={}, brokerId={}, clientId={}, cityId={}",
                saved.getId(), broker.getId(), client.getId(), city.getId());

        return buildingMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    @Transactional(readOnly = true)
    public BuildingResponseDTO getBuildingById(Long buildingId, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);
        log.info("Get building by id: brokerId={}, buildingId={}", broker.getId(), buildingId);

        return buildingMapper.toResponse(findBuildingOrThrow(buildingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingResponseDTO> getBuildingsByClientId(Long clientId, Pageable pageable, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);

        log.info("Get buildings by clientId: brokerId={}, clientId={}, page={}, size={}, sort={}",
                broker.getId(), clientId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        findClientOrThrow(clientId);

        return buildingRepository.findByClientId(clientId, pageable)
                .map(buildingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailsResponseDTO getBuildingDetailsById(Long buildingId, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);
        log.info("Get building details by id: brokerId={}, buildingId={}", broker.getId(), buildingId);

        Building building = buildingRepository.findDetailsById(buildingId)
                .orElseThrow(() -> {
                    log.warn("Building not found for fetching details: id={}", buildingId);
                    return new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
                });

        return buildingMapper.toDetailsResponse(building);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public BuildingDetailsResponseDTO updateBuilding(Long buildingId, BuildingUpdateRequestDTO dto, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);

        log.info("Update building: brokerId={}, buildingId={}, newCityId={}", broker.getId(), buildingId, dto.cityId());

        Building building = findBuildingOrThrow(buildingId);
        City city = findCityOrThrow(dto.cityId());

        Long oldCityId = building.getCity() != null ? building.getCity().getId() : null;

        buildingMapper.updateFromDto(dto, city, building);

        Building saved = buildingRepository.save(building);

        log.info("Building updated: id={}, cityId {} -> {}", saved.getId(), oldCityId, city.getId());

        return buildingMapper.toDetailsResponse(saved);
    }

    // ---------------------  PATCH  ---------------------

    @Override
    public BuildingResponseDTO patchBuilding(Long buildingId, BuildingPatchDTO dto, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);

        log.info("Patch building: brokerId={}, buildingId={}, cityId={}",
                broker.getId(), buildingId, dto.cityId());

        Building building = findBuildingOrThrow(buildingId);
        City city = (dto.cityId() == null) ? null : findCityOrThrow(dto.cityId());

        buildingMapper.patchFromDto(dto, city, building);

        Building saved = buildingRepository.save(building);

        log.info("Building patched: id={}, cityId={}", saved.getId(), saved.getCity() != null ? saved.getCity().getId() : null);

        return buildingMapper.toResponse(saved);
    }

    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteBuilding(Long buildingId, String brokerEmail) {
        Broker broker = currentBrokerProvider.getActiveOrThrow(brokerEmail);
        log.warn("Delete building requested: brokerId={}, buildingId={}", broker.getId(), buildingId);

        Building building = findBuildingOrThrow(buildingId);

        try {
            buildingRepository.delete(building);
            log.info("Building deleted: id={}", buildingId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete building failed - has dependencies: id={}", buildingId);
            throw new BusinessException(ErrorCode.BUILDING_HAS_DEPENDENCIES);
        }
    }

    // ------------------------ HELPERS ------------------------

    private Building findBuildingOrThrow(Long buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> {
                    log.warn("Building not found: id={}", buildingId);
                    return new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
                });
    }

    private Client findClientOrThrow(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client not found: id={}", clientId);
                    return new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
                });
    }

    private City findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> {
                    log.warn("City not found: id={}", cityId);
                    return new BusinessException(ErrorCode.CITY_NOT_FOUND);
                });
    }
}

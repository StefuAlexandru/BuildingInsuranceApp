package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.domain.entity.Building;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.dto.buildingDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.BuildingMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BuildingRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientRepository;
import ro.insurance.buildinginsurance.application.service.IBuildingService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BuildingService implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final ClientRepository clientRepository;
    private final CityRepository cityRepository;
    private final BuildingMapper buildingMapper;

    // ---------------------  CREATE  ---------------------

    @Override
    public BuildingResponseDTO registerNewBuilding(BuildingRequestDTO buildingRequestDTO) {
        log.info("Register building : clientId={},cityId={}",buildingRequestDTO.clientId(),buildingRequestDTO.cityId());
        Client client = findClientOrThrow(buildingRequestDTO.clientId());
        City city = findCityOrThrow(buildingRequestDTO.cityId());

        Building building = buildingMapper.toEntity(buildingRequestDTO, client, city);
        Building saved = buildingRepository.save(building);

        log.info("Building registered: id={}, clientId={}, cityId={}", saved.getId(), client.getId(), city.getId());

        return buildingMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    @Transactional(readOnly = true)
    public BuildingResponseDTO getBuildingById(Long buildingId) {
        log.info("Get building by id: {}", buildingId);
        return buildingMapper.toResponse(findBuildingOrThrow(buildingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingResponseDTO> getBuildingsByClientId(Long clientId, Pageable pageable) {
        log.info("Get buildings by clientId={}, page={}, sort={}, sort={}", clientId,pageable.getPageNumber(),pageable.getPageSize(),pageable.getSort());
        findClientOrThrow(clientId);

        return buildingRepository.findByClientId(clientId, pageable)
                .map(buildingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailsResponseDTO getBuildingDetailsById(Long buildingId) {
        log.info("Get building details by id: {}", buildingId);
        Building building = buildingRepository.findDetailsById(buildingId)
                .orElseThrow(() -> {
                    log.warn("Building not found for fetching details : id={}", buildingId);
                    return new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
                });

        return buildingMapper.toDetailsResponse(building);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public BuildingDetailsResponseDTO updateBuilding(Long buildingId, BuildingUpdateRequestDTO buildingUpdateRequestDTO) {
        log.info("Update building: id={}, newCityId={}",buildingId,buildingUpdateRequestDTO.cityId());
        Building building = findBuildingOrThrow(buildingId);
        City city = findCityOrThrow(buildingUpdateRequestDTO.cityId());

        Long oldCityId = building.getCity() != null ? building.getCity().getId() : null;

        buildingMapper.updateFromDto(buildingUpdateRequestDTO, city, building);

        Building saved = buildingRepository.save(building);

        log.info("Building updated: id={}, cityId {} -> {}", saved.getId(), oldCityId, city.getId());

        return buildingMapper.toDetailsResponse(saved);
    }

    // ---------------------  PATCH  ---------------------

    @Override
    public BuildingResponseDTO patchBuilding(Long buildingId, BuildingPatchDTO dto) {
        log.info("Patch building: id={}, cityId={}", buildingId, dto.cityId());
        Building building = findBuildingOrThrow(buildingId);

        City city = (dto.cityId() == null) ? null : findCityOrThrow(dto.cityId());

        buildingMapper.patchFromDto(dto, city, building);

        Building saved = buildingRepository.save(building);

        log.info("Building patched: id={}, cityId={}", saved.getId(), saved.getCity() != null ? saved.getCity().getId() : null);

        return buildingMapper.toResponse(saved);
    }

    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteBuilding(Long buildingId) {
        log.warn("Delete building requested: id={}", buildingId);
        try {
            buildingRepository.deleteById(buildingId);
            log.info("Building deleted: id={}", buildingId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete building failed - not found: id={}", buildingId);
            throw new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
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

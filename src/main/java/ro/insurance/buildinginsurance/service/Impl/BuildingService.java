package ro.insurance.buildinginsurance.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.buildingDto.*;
import ro.insurance.buildinginsurance.entity.Building;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.mapper.BuildingMapper;
import ro.insurance.buildinginsurance.repository.BuildingRepository;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.ClientRepository;
import ro.insurance.buildinginsurance.service.IBuildingService;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildingService implements IBuildingService {

    private final BuildingRepository buildingRepository;
    private final ClientRepository clientRepository;
    private final CityRepository cityRepository;
    private final BuildingMapper buildingMapper;


    @Override
    public BuildingResponseDTO registerNewBuilding(BuildingRequestDTO buildingRequestDTO) {
        Client client = findClientOrThrow(buildingRequestDTO.clientId());
        City city = findCityOrThrow(buildingRequestDTO.cityId());

        Building building = buildingMapper.toEntity(buildingRequestDTO, client, city);
        Building saved = buildingRepository.save(building);

        return buildingMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    @Transactional(readOnly = true)
    public BuildingResponseDTO getBuildingById(Long buildingId) {
        return buildingMapper.toResponse(findBuildingOrThrow(buildingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingResponseDTO> getBuildingsByClientId(Long clientId, Pageable pageable) {
        findClientOrThrow(clientId);

        return buildingRepository.findByClientId(clientId, pageable)
                .map(buildingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingDetailsResponseDTO getBuildingDetailsById(Long buildingId) {
        Building building = buildingRepository.findDetailsById(buildingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));

        return buildingMapper.toDetailsResponse(building);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public BuildingResponseDTO updateBuilding(Long buildingId, BuildingUpdateRequestDTO dto) {
        Building building = findBuildingOrThrow(buildingId);
        City city = findCityOrThrow(dto.cityId());

        buildingMapper.updateFromDto(dto, city, building);

        Building saved = buildingRepository.save(building);
        return buildingMapper.toResponse(saved);
    }

    // ---------------------  PATCH  ---------------------

    @Override
    public BuildingResponseDTO patchBuilding(Long buildingId, BuildingPatchDTO dto) {
        Building building = findBuildingOrThrow(buildingId);

        City city = (dto.cityId() == null) ? null : findCityOrThrow(dto.cityId());

        buildingMapper.patchFromDto(dto, city, building);

        Building saved = buildingRepository.save(building);
        return buildingMapper.toResponse(saved);
    }

    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteBuilding(Long buildingId) {
        try {
            buildingRepository.deleteById(buildingId);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.BUILDING_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.BUILDING_HAS_DEPENDENCIES);
        }
    }

    // ------------------------ HELPERS ------------------------

    private Building findBuildingOrThrow(Long buildingId) {
        return buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUILDING_NOT_FOUND));
    }

    private Client findClientOrThrow(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));
    }

    private City findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
    }
}

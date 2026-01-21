package ro.insurance.buildinginsurance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.dto.buildingDto.*;

public interface IBuildingService {
    BuildingResponseDTO registerNewBuilding(BuildingRequestDTO dto);

    BuildingResponseDTO getBuildingById(Long buildingId);
    Page<BuildingResponseDTO> getBuildingsByClientId(Long clientId, Pageable pageable);
    BuildingDetailsResponseDTO getBuildingDetailsById(Long buildingId);

    BuildingDetailsResponseDTO updateBuilding(Long buildingId, BuildingUpdateRequestDTO dto);

    BuildingResponseDTO patchBuilding(Long buildingId, BuildingPatchDTO dto);

    void deleteBuilding(Long buildingId);
}

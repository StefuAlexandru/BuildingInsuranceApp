package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;

public interface IBuildingService {
    BuildingResponseDTO registerNewBuilding(BuildingRequestDTO dto, String brokerEmail);

    BuildingResponseDTO getBuildingById(Long buildingId, String brokerEmail);
    Page<BuildingResponseDTO> getBuildingsByClientId(Long clientId, Pageable pageable, String brokerEmail);
    BuildingDetailsResponseDTO getBuildingDetailsById(Long buildingId, String brokerEmail);

    BuildingDetailsResponseDTO updateBuilding(Long buildingId, BuildingUpdateRequestDTO dto, String brokerEmail);

    BuildingResponseDTO patchBuilding(Long buildingId, BuildingPatchDTO dto, String brokerEmail);

    void deleteBuilding(Long buildingId, String brokerEmail);
}

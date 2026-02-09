package ro.insurance.buildinginsurance.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.insurance.buildinginsurance.web.dto.client_dto.*;


public interface IClientService {
    ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO);

    ClientResponseDTO getClientById(Long clientId);
    ClientDetailsResponseDTO getClientDetailsById(Long clientId);
    ClientResponseDTO getClientByNameAndIdentificationNumber(String name,String identificationNumber);
    Page<ClientResponseDTO> getAllClients(Pageable pageable);

    Page<ClientResponseDTO> searchClients(String name, String identificationNumber, Pageable pageable);

    ClientResponseDTO updateClient(Long clientId, ClientUpdateRequestDTO clientUpdateRequestDTO);

    ClientResponseDTO patchIdentity(Long clientId, ClientIdentityPatchDTO clientIdentityPatchDTO, String changedBy);

    void deleteClient(Long clientId);
}

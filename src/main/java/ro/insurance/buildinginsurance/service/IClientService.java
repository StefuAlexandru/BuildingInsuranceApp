package ro.insurance.buildinginsurance.service;

import ro.insurance.buildinginsurance.dto.clientDto.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IClientService {
    ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO);

    ClientResponseDTO getClientById(Long clientId);
    ClientResponseDTO getClientByNameAndIdentificationNumber(String name,String identificationNumber);
    Page<ClientResponseDTO> getAllClients(Pageable pageable);

    ClientResponseDTO updateClient(Long clientId, ClientUpdateRequestDTO clientUpdateRequestDTO);

    ClientResponseDTO patchIdentity(Long clientId, ClientIdentityPatchDTO clientIdentityPatchDTO,String changedBy);

    void deleteClient(Long clientId);
}

package ro.insurance.buildinginsurance.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.dto.clientDto.ClientIdentityPatchDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.error.ErrorModel;
import ro.insurance.buildinginsurance.facade.ClientIdentityFacade;
import ro.insurance.buildinginsurance.mapper.ClientMapper;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.ClientRepository;
import ro.insurance.buildinginsurance.service.IClientService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;
    private final CityRepository cityRepository;
    private final ClientMapper clientMapper;
    private final ClientIdentityFacade clientIdentityFacade;


    // ---------------------  CREATE  ---------------------

    @Override
    public ClientResponseDTO createClient(ClientRequestDTO clientRequestDTO) {
        validateCreate(clientRequestDTO);

        City city = resolveAddressCityOrNull(clientRequestDTO.addressCityId(), clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber());

        Client client = clientMapper.toEntity(clientRequestDTO, city);
        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long clientId) {
        return clientMapper.toResponse(findClientOrThrow(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByNameAndIdentificationNumber(String name, String identificationNumber) {
        Client client = clientRepository.findByNameAndIdentificationNumber(name.trim(),identificationNumber.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));

        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(clientMapper::toResponse);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public ClientResponseDTO updateClient(Long clientId, ClientUpdateRequestDTO clientUpdateRequestDTO) {
        Client client = findClientOrThrow(clientId);

        City city = resolveAddressCityOrNull(clientUpdateRequestDTO.addressCityId(), clientUpdateRequestDTO.addressStreet(), clientUpdateRequestDTO.addressNumber());

        clientMapper.updateFromDto(clientUpdateRequestDTO, city, client);

        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }


    // ---------------------  PATCH  ---------------------

    public ClientResponseDTO patchIdentity(Long clientId, ClientIdentityPatchDTO dto, String changedBy) {
        Client saved = clientIdentityFacade.patchIdentity(clientId, dto, changedBy);
        return clientMapper.toResponse(saved);
    }


    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteClient(Long clientId) {
        try {
            clientRepository.deleteById(clientId);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CLIENT_HAS_DEPENDENCIES);
        }
    }


    // ------------------------ HELPERS ------------------------

    private void validateCreate(ClientRequestDTO clientRequestDTO) {
        List<ErrorModel> errors = new ArrayList<>();

        if (clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())) {
            errors.add(new ErrorModel(ErrorCode.CLIENT_ALREADY_EXIST));
        }

        if (hasAnyAddress(clientRequestDTO.addressCityId(), clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber())
                && !hasFullAddress(clientRequestDTO.addressCityId(), clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber())) {
            errors.add(new ErrorModel(ErrorCode.ADDRESS_INCOMPLETE));
        }

        if (!errors.isEmpty()) throw new BusinessException(errors);
    }

    private City resolveAddressCityOrNull(Long cityId, String street, String number) {
        if (!hasAnyAddress(cityId, street, number)) return null;

        if (!hasFullAddress(cityId, street, number)) {
            throw new BusinessException(ErrorCode.ADDRESS_INCOMPLETE);
        }

        return cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
    }

    private boolean hasAnyAddress(Long cityId, String street, String number) {
        return cityId != null
                || (street != null && !street.isBlank())
                || (number != null && !number.isBlank());
    }

    private boolean hasFullAddress(Long cityId, String street, String number) {
        return cityId != null
                && (street != null && !street.isBlank())
                && (number != null && !number.isBlank());
    }

    private Client findClientOrThrow(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));
    }
}



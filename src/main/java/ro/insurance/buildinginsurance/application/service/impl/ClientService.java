package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.dto.clientDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.web.error.ErrorModel;
import ro.insurance.buildinginsurance.application.facade.ClientIdentityFacade;
import ro.insurance.buildinginsurance.application.mapper.ClientMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientRepository;
import ro.insurance.buildinginsurance.application.service.IClientService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        log.info("Create client requested: clientType={}, hasAddress={}",
                clientRequestDTO.clientType(), hasAnyAddress(clientRequestDTO.addressCityId(),
                clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber()));
        validateCreate(clientRequestDTO);

        City city = resolveAddressCityOrNull(clientRequestDTO.addressCityId(), clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber());
        Client client = clientMapper.toEntity(clientRequestDTO, city);
        Client saved = clientRepository.save(client);

        log.info("Client created: id={}, clientType={}, cityId={}",
                saved.getId(), saved.getClientType(),
                saved.getAddressCity() != null ? saved.getAddressCity().getId() : null);
        return clientMapper.toResponse(saved);
    }

    // ---------------------  GET  ---------------------

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long clientId) {
        log.info("Get client by id: {}", clientId);
        return clientMapper.toResponse(findClientOrThrow(clientId));
    }

    @Transactional(readOnly = true)
    public ClientDetailsResponseDTO getClientDetailsById(Long clientId) {
        log.info("Get client details by id: {}", clientId);
        Client client = clientRepository.findWithBuildingsById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client not found : id={}", clientId);
                    return new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
                });

        return clientMapper.toDetailsResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByNameAndIdentificationNumber(String name, String identificationNumber) {
        log.info("Lookup client by name+identificationNumber");

        Client client = clientRepository.findByNameIgnoreCaseAndIdentificationNumber(name.trim(),identificationNumber.trim())
                .orElseThrow(() -> {
                    log.warn("Client not found (lookup by name + identificationNumber)");
                    return new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
                });

        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClients(Pageable pageable) {
        log.info("Get all clients: page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return clientRepository.findAll(pageable)
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> searchClients(String name, String identificationNumber, Pageable pageable) {
        String nameFilter = (name == null || name.isBlank()) ? null : name.trim();
        String identificationNumberFilter = (identificationNumber == null || identificationNumber.isBlank()) ? null : identificationNumber.trim();

        log.info("Search clients: hasNameFilter={}, hasIdentificationNumberFilter={}, page={}, size={}",
                nameFilter != null, identificationNumberFilter != null, pageable.getPageNumber(), pageable.getPageSize());

        return clientRepository.search(nameFilter, identificationNumberFilter, pageable)
                .map(clientMapper::toResponse);
    }

    // ---------------------  UPDATE  ---------------------

    @Override
    public ClientResponseDTO updateClient(Long clientId, ClientUpdateRequestDTO clientUpdateRequestDTO) {
        log.info("Update client requested: id={}, hasAddress={}",
                clientId, hasAnyAddress(clientUpdateRequestDTO.addressCityId(), clientUpdateRequestDTO.addressStreet(), clientUpdateRequestDTO.addressNumber()));
        Client client = findClientOrThrow(clientId);

        City city = resolveAddressCityOrNull(clientUpdateRequestDTO.addressCityId(), clientUpdateRequestDTO.addressStreet(), clientUpdateRequestDTO.addressNumber());
        clientMapper.updateFromDto(clientUpdateRequestDTO, city, client);

        Client saved = clientRepository.save(client);
        log.info("Client updated: id={}, cityId={}", saved.getId(), saved.getAddressCity() != null ? saved.getAddressCity().getId() : null);

        return clientMapper.toResponse(saved);
    }


    // ---------------------  PATCH  ---------------------

    public ClientResponseDTO patchIdentity(Long clientId, ClientIdentityPatchDTO dto, String changedBy) {
        log.info("Patch client identity requested: clientId={}, actor={}", clientId, changedBy);
        Client saved = clientIdentityFacade.patchIdentity(clientId, dto, changedBy);
        log.info("Patch client identity done: clientId={}", saved.getId());
        return clientMapper.toResponse(saved);
    }


    // ---------------------  DELETE  ---------------------

    @Override
    public void deleteClient(Long clientId) {
        log.warn("Delete client requested: id={}", clientId);
        try {
            clientRepository.deleteById(clientId);
            log.info("Client deleted: id={}", clientId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete client failed - not found: id={}", clientId);
            throw new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.warn("Delete client failed - has dependencies: id={}", clientId);
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

        if (!errors.isEmpty()) {
            log.warn("Create client validation failed: errors={}", errors);
            throw new BusinessException(errors);
        }
    }

    private City resolveAddressCityOrNull(Long cityId, String street, String number) {
        if (!hasAnyAddress(cityId, street, number)) return null;

        if (!hasFullAddress(cityId, street, number)) {
            log.warn("Address incomplete: cityIdPresent={}, streetPresent={}, numberPresent={}",
                    cityId != null, street != null && !street.isBlank(), number != null && !number.isBlank());
            throw new BusinessException(ErrorCode.ADDRESS_INCOMPLETE);
        }

        return cityRepository.findById(cityId)
                .orElseThrow(() -> {
                    log.warn("Address city not found: cityId={}", cityId);
                    return new BusinessException(ErrorCode.CITY_NOT_FOUND);
                });
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
                .orElseThrow(() -> {
                    log.warn("Client not found: id={}", clientId);
                    return new BusinessException(ErrorCode.CLIENT_NOT_FOUND);
                });
    }
}



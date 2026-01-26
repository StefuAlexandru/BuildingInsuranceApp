package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import ro.insurance.buildinginsurance.application.service.impl.ClientService;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.dto.clientDto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.facade.ClientIdentityFacade;
import ro.insurance.buildinginsurance.application.mapper.ClientMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.*;


@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private ClientIdentityFacade clientIdentityFacade;

    @InjectMocks
    private ClientService clientService;


    // ---------------------  CREATE  ---------------------

    @Test
    void createClient_shouldCreateAndReturnResponse() {
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequest();
        City city = aCity();

        Client entity = new Client();
        Client saved = new Client();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);
        when(cityRepository.findById(clientRequestDTO.addressCityId())).thenReturn(Optional.of(city));
        when(clientMapper.toEntity(clientRequestDTO, city)).thenReturn(entity);
        when(clientRepository.save(entity)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.createClient(clientRequestDTO);

        // Assert
        assertSame(response, result);

        verify(cityRepository).findById(clientRequestDTO.addressCityId());
        verify(clientMapper).toEntity(clientRequestDTO, city);
        verify(clientRepository).save(entity);
        verify(clientMapper).toResponse(saved);

        verifyNoInteractions(clientIdentityFacade);
        verifyNoMoreInteractions(cityRepository, clientMapper, clientRepository);
    }

    @Test
    void createClient_shouldThrowWhenClientAlreadyExists(){
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequest();

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(true);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_ALREADY_EXIST.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim());
        verify(clientRepository,never()).save(any());
        verifyNoInteractions(cityRepository,clientMapper,clientIdentityFacade);
    }

    @Test
    void createClient_shouldThrowWhenNumberStreetMissing(){
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequestWithAddress(1L, "Str. Gheo", null);

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.ADDRESS_INCOMPLETE.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(cityRepository, clientMapper,clientIdentityFacade);
    }

    @Test
    void createClient_shouldThrowWhenStreetNameMissing(){
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequestWithAddress(1L, null, "10A");

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.ADDRESS_INCOMPLETE.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(cityRepository, clientMapper,clientIdentityFacade);
    }

    @Test
    void createClient_shouldThrowWhenCityIdMissing(){
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequestWithAddress(null, "Str. Gheo", "10A");

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.ADDRESS_INCOMPLETE.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(cityRepository, clientMapper,clientIdentityFacade);
    }

    @Test
    void createClient_shouldCreateWhenNoAddressIsProvided(){
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequestWithAddress(null, null, null);

        Client entity = new Client();
        Client saved = new Client();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);
        when(clientMapper.toEntity(clientRequestDTO, null)).thenReturn(entity);
        when(clientRepository.save(entity)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.createClient(clientRequestDTO);

        // Assert
        assertSame(response, result);

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim());
        verify(clientMapper).toEntity(clientRequestDTO, null);
        verify(clientRepository).save(entity);
        verify(clientMapper).toResponse(saved);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository, clientMapper);
    }

    @Test
    void createClient_shouldThrowWhenCityNotFound() {
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequest();

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);
        when(cityRepository.findById(clientRequestDTO.addressCityId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(cityRepository).findById(clientRequestDTO.addressCityId());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(clientMapper, clientIdentityFacade);
    }


    // ---------------------  UPDATE  ---------------------

    @Test
    void updateClient_shouldUpdateAndReturnResponse() {
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequest();
        Client client = aClient();
        City city = aCityWithId(clientUpdateRequestDTO.addressCityId());

        Client saved = new Client();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(cityRepository.findById(clientUpdateRequestDTO.addressCityId())).thenReturn(Optional.of(city));
        when(clientRepository.save(client)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.updateClient(clientId, clientUpdateRequestDTO);

        // Assert
        assertSame(response, result);

        verify(clientRepository).findById(clientId);
        verify(cityRepository).findById(clientUpdateRequestDTO.addressCityId());
        verify(clientMapper).updateFromDto(clientUpdateRequestDTO, city, client);
        verify(clientRepository).save(client);
        verify(clientMapper).toResponse(saved);

        verifyNoInteractions(clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository, cityRepository, clientMapper);
    }

    @Test
    void updateClient_shouldThrowWhenClientNotFound(){
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequest();

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.updateClient(clientId, clientUpdateRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(cityRepository, clientMapper, clientIdentityFacade);
    }

    @Test
    void updateClient_shouldAllowWhenNoAddressProvided() {
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequestWithAddress(null, null, null);

        Client client = aClient();
        Client saved = new Client();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.updateClient(clientId, clientUpdateRequestDTO);

        // Assert
        assertSame(response, result);

        verify(clientRepository).findById(clientId);
        verify(clientMapper).updateFromDto(clientUpdateRequestDTO, null, client);
        verify(clientRepository).save(client);
        verify(clientMapper).toResponse(saved);

        verifyNoInteractions(cityRepository, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository, clientMapper);
    }

    @Test
    void updateClient_shouldThrowWhenAddressIncomplete() {
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequestWithAddress(2L, "Str. Vasi", null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(aClient()));

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.updateClient(clientId, clientUpdateRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.ADDRESS_INCOMPLETE.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(clientId);
        verifyNoInteractions(cityRepository, clientMapper, clientIdentityFacade);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClient_shouldThrowWhenCityNotFound() {
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequestWithAddress(2L, "Str. Vasi", "20B");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(aClient()));
        when(cityRepository.findById(clientUpdateRequestDTO.addressCityId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.updateClient(clientId, clientUpdateRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(clientId);
        verify(cityRepository).findById(clientUpdateRequestDTO.addressCityId());
        verifyNoInteractions(clientMapper, clientIdentityFacade);
        verify(clientRepository, never()).save(any());
    }


    // ---------------------  GET  ---------------------


    @Test
    void getClientById_shouldReturnResponse() {
        // Arrange
        Long id = 1L;
        Client client = aClient();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.getClientById(id);

        // Assert
        assertSame(response, result);

        verify(clientRepository).findById(id);
        verify(clientMapper).toResponse(client);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository, clientMapper);
    }

    @Test
    void getClientById_shouldThrowWhenNotFound() {
        // Arrange
        Long id = 1L;

        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.getClientById(id));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(id);
        verifyNoInteractions(clientMapper, cityRepository, clientIdentityFacade);
    }

    @Test
    void getClientByNameAndIdentificationNumber_shouldTrimAndReturnResponse() {
        // Arrange
        String name = "  Old Name  ";
        String idNo = "  OLD-CNP  ";

        Client client = aClient();
        ClientResponseDTO response = aClientResponse();

        when(clientRepository.findByNameIgnoreCaseAndIdentificationNumber("Old Name", "OLD-CNP")).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.getClientByNameAndIdentificationNumber(name, idNo);

        // Assert
        assertSame(response, result);

        verify(clientRepository).findByNameIgnoreCaseAndIdentificationNumber("Old Name", "OLD-CNP");
        verify(clientMapper).toResponse(client);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
    }

    @Test
    void getClientByNameAndIdentificationNumber_shouldThrowWhenNotFound() {
        // Arrange
        when(clientRepository.findByNameIgnoreCaseAndIdentificationNumber("X", "Y")).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.getClientByNameAndIdentificationNumber(" X ", " Y "));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );


        verify(clientRepository).findByNameIgnoreCaseAndIdentificationNumber("X", "Y");
        verifyNoInteractions(clientMapper, cityRepository, clientIdentityFacade);
    }

    @Test
    void searchClients_shouldTrimAndMap() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Client client = aClient();
        ClientResponseDTO dto = aClientResponse();

        Page<Client> repoPage = new PageImpl<>(List.of(client), pageable, 1);

        when(clientRepository.search("Gheo", "1960101123456", pageable)).thenReturn(repoPage);
        when(clientMapper.toResponse(client)).thenReturn(dto);

        // Act
        Page<ClientResponseDTO> result = clientService.searchClients("  Gheo  ", "  1960101123456  ", pageable);

        assertAll(
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals(1, result.getContent().size()),
                () -> assertSame(dto, result.getContent().getFirst())
        );

        verify(clientRepository).search("Gheo", "1960101123456", pageable);
        verify(clientMapper).toResponse(client);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
    }

    @Test
    void getAllClients_shouldMapToResponsePage() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 2);

        Client client1 = aClient();
        Client client2 = aClient();
        Page<Client> page = new PageImpl<>(List.of(client1, client2), pageable, 2);

        ClientResponseDTO clientResponseDTO1 = aClientResponse();
        ClientResponseDTO clientResponseDTO2 = aClientResponse();

        when(clientRepository.findAll(pageable)).thenReturn(page);
        when(clientMapper.toResponse(client1)).thenReturn(clientResponseDTO1);
        when(clientMapper.toResponse(client2)).thenReturn(clientResponseDTO2);

        // Act
        Page<ClientResponseDTO> result = clientService.getAllClients(pageable);

        // Assert
        assertAll(
                () -> assertEquals(2, result.getContent().size()),
                () -> assertSame(clientResponseDTO1, result.getContent().getFirst()),
                () -> assertSame(clientResponseDTO2, result.getContent().get(1))
        );

        verify(clientRepository).findAll(pageable);
        verify(clientMapper).toResponse(client1);
        verify(clientMapper).toResponse(client2);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
    }

    @Test
    void getClientDetailsById_shouldReturnDetails() {
        // Arrange
        Long clientId = 1L;
        Client client = new Client();
        ClientDetailsResponseDTO dto = aClientDetailsResponse();

        when(clientRepository.findWithBuildingsById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toDetailsResponse(client)).thenReturn(dto);

        // Act
        ClientDetailsResponseDTO result = clientService.getClientDetailsById(clientId);

        // Assert
        assertSame(dto, result);

        verify(clientRepository).findWithBuildingsById(clientId);
        verify(clientMapper).toDetailsResponse(client);
        verifyNoInteractions(cityRepository, clientIdentityFacade);
    }

    @Test
    void getClientDetailsById_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        Long clientId = 99L;
        when(clientRepository.findWithBuildingsById(clientId)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.getClientDetailsById(clientId));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findWithBuildingsById(clientId);
        verifyNoInteractions(clientMapper, cityRepository, clientIdentityFacade);
    }


    // ---------------------  PATCH  ---------------------


    @Test
    void patchIdentity_shouldDelegateToFacadeAndMapResponse() {
        // Arrange
        Long clientId = 1L;
        ClientIdentityPatchDTO dto = aClientIdentityPatchDTO();
        String changedBy = "  alex  ";

        Client saved = new Client();
        ClientResponseDTO response = aClientResponse();

        when(clientIdentityFacade.patchIdentity(clientId, dto, changedBy)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        // Act
        ClientResponseDTO result = clientService.patchIdentity(clientId, dto, changedBy);

        // Assert
        assertSame(response, result);

        verify(clientIdentityFacade).patchIdentity(clientId, dto, changedBy);
        verify(clientMapper).toResponse(saved);
        verifyNoMoreInteractions(clientIdentityFacade, clientMapper);
        verifyNoInteractions(clientRepository, cityRepository);
    }


    // ---------------------  DELETE  ---------------------


    @Test
    void deleteClient_shouldDeleteSuccessfully() {
        // Arrange
        Long id = 1L;

        doNothing().when(clientRepository).deleteById(id);

        // Act + Assert
        assertDoesNotThrow(() -> clientService.deleteClient(id));

        verify(clientRepository).deleteById(id);
        verifyNoInteractions(cityRepository, clientMapper, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void deleteClient_shouldThrowWhenClientNotFound() {
        // Arrange
        Long id = 1L;

        doThrow(new EmptyResultDataAccessException(1)).when(clientRepository).deleteById(id);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.deleteClient(id));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).deleteById(id);
        verifyNoInteractions(cityRepository, clientMapper, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void deleteClient_shouldThrowWhenClientHasDependencies() {
        // Arrange
        Long id = 1L;

        doThrow(new DataIntegrityViolationException("fk")).when(clientRepository).deleteById(id);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.deleteClient(id));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_HAS_DEPENDENCIES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).deleteById(id);
        verifyNoInteractions(cityRepository, clientMapper, clientIdentityFacade);
        verifyNoMoreInteractions(clientRepository);
    }












}
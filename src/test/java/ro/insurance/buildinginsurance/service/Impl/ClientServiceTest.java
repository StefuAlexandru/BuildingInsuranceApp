package ro.insurance.buildinginsurance.service.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ErrorCode;
import ro.insurance.buildinginsurance.error.BusinessException;
import ro.insurance.buildinginsurance.facade.ClientIdentityFacade;
import ro.insurance.buildinginsurance.mapper.ClientMapper;
import ro.insurance.buildinginsurance.repository.CityRepository;
import ro.insurance.buildinginsurance.repository.ClientRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
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

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber());
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

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber());
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

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber());
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

        verify(clientRepository).existsByIdentificationNumber(clientRequestDTO.identificationNumber());
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
        ClientRequestDTO clientRequestDTO = aClientRequest();

        when(clientRepository.existsByIdentificationNumber(clientRequestDTO.identificationNumber().trim())).thenReturn(false);
        when(cityRepository.findById(clientRequestDTO.addressCityId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> clientService.createClient(clientRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(cityRepository).findById(clientRequestDTO.addressCityId());
        verify(clientRepository, never()).save(any());
        verifyNoInteractions(clientMapper, clientIdentityFacade);
    }






}
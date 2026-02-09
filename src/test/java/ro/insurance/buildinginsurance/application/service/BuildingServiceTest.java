package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import ro.insurance.buildinginsurance.application.mapper.BuildingMapper;
import ro.insurance.buildinginsurance.application.security.CurrentBrokerProvider;
import ro.insurance.buildinginsurance.application.service.impl.BuildingService;
import ro.insurance.buildinginsurance.domain.entity.*;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.*;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.BrokerTestDataFactory.aBrokerWithId;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClient;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock private BuildingRepository buildingRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CityRepository cityRepository;
    @Mock private BuildingMapper buildingMapper;
    @Mock private CurrentBrokerProvider currentBrokerProvider;

    @InjectMocks private BuildingService buildingService;

    private static final String EMAIL = "broker@email.com";

    // ---------------------  CREATE  ---------------------

    @Test
    void registerNewBuilding_shouldMapSaveAndReturnResponse() {
        BuildingRequestDTO dto = aBuildingRequest();
        Client client = aClient();
        City city = aCityWithId(dto.cityId());
        Broker broker = aBrokerWithId(1L);

        Building building = new Building();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(clientRepository.findById(dto.clientId())).thenReturn(Optional.of(client));
        when(cityRepository.findById(dto.cityId())).thenReturn(Optional.of(city));
        when(buildingMapper.toEntity(dto, client, city)).thenReturn(building);
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        BuildingResponseDTO result = buildingService.registerNewBuilding(dto, EMAIL);

        assertSame(response, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(clientRepository).findById(dto.clientId());
        verify(cityRepository).findById(dto.cityId());
        verify(buildingMapper).toEntity(dto, client, city);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoMoreInteractions(currentBrokerProvider, clientRepository, cityRepository, buildingRepository, buildingMapper);
    }

    @Test
    void registerNewBuilding_shouldThrowWhenClientNotFound() {
        BuildingRequestDTO dto = aBuildingRequest();
        Broker broker = aBrokerWithId(1L);

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(clientRepository.findById(dto.clientId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.registerNewBuilding(dto, EMAIL));

        assertHasError(ex, ErrorCode.CLIENT_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(clientRepository).findById(dto.clientId());
        verifyNoInteractions(cityRepository, buildingRepository, buildingMapper);
        verifyNoMoreInteractions(currentBrokerProvider, clientRepository);
    }

    @Test
    void registerNewBuilding_shouldThrowWhenCityNotFound() {
        BuildingRequestDTO dto = aBuildingRequest();
        Broker broker = aBrokerWithId(1L);
        Client client = aClient();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(clientRepository.findById(dto.clientId())).thenReturn(Optional.of(client));
        when(cityRepository.findById(dto.cityId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.registerNewBuilding(dto, EMAIL));

        assertHasError(ex, ErrorCode.CITY_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(clientRepository).findById(dto.clientId());
        verify(cityRepository).findById(dto.cityId());
        verifyNoInteractions(buildingRepository, buildingMapper);
        verifyNoMoreInteractions(currentBrokerProvider, clientRepository, cityRepository);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getBuildingById_shouldReturnResponse() {
        Broker broker = aBrokerWithId(1L);
        Building building = aBuilding();
        BuildingResponseDTO response = aBuildingResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(buildingMapper.toResponse(building)).thenReturn(response);

        BuildingResponseDTO result = buildingService.getBuildingById(1L, EMAIL);

        assertSame(response, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(buildingMapper).toResponse(building);

        verifyNoInteractions(clientRepository, cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, buildingMapper);
    }

    @Test
    void getBuildingById_shouldThrowWhenNotFound() {
        Broker broker = aBrokerWithId(1L);

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.getBuildingById(1L, EMAIL));

        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verifyNoInteractions(buildingMapper, clientRepository, cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository);
    }

    @Test
    void getBuildingsByClientId_shouldReturnPage() {
        Broker broker = aBrokerWithId(1L);

        long clientId = 1L;
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").ascending());

        Client client = aClient();
        Building building = aBuilding();
        Page<Building> page = new PageImpl<>(List.of(building), pageable, 1);

        BuildingResponseDTO response = aBuildingResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(buildingRepository.findByClientId(clientId, pageable)).thenReturn(page);
        when(buildingMapper.toResponse(building)).thenReturn(response);

        Page<BuildingResponseDTO> result = buildingService.getBuildingsByClientId(clientId, pageable, EMAIL);

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().getFirst());

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(clientRepository).findById(clientId);
        verify(buildingRepository).findByClientId(clientId, pageable);
        verify(buildingMapper).toResponse(building);

        verifyNoInteractions(cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, clientRepository, buildingRepository, buildingMapper);
    }

    @Test
    void getBuildingsByClientId_shouldThrowWhenClientNotFound() {
        Broker broker = aBrokerWithId(1L);

        long clientId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.getBuildingsByClientId(clientId, pageable, EMAIL));

        assertHasError(ex, ErrorCode.CLIENT_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(clientRepository).findById(clientId);

        verifyNoInteractions(buildingRepository, buildingMapper, cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, clientRepository);
    }

    @Test
    void getBuildingDetailsById_shouldReturnDetailsResponse() {
        Broker broker = aBrokerWithId(1L);
        Building building = aBuilding();
        BuildingDetailsResponseDTO details = aBuildingDetailsResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findDetailsById(1L)).thenReturn(Optional.of(building));
        when(buildingMapper.toDetailsResponse(building)).thenReturn(details);

        BuildingDetailsResponseDTO result = buildingService.getBuildingDetailsById(1L, EMAIL);

        assertSame(details, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findDetailsById(1L);
        verify(buildingMapper).toDetailsResponse(building);

        verifyNoInteractions(clientRepository, cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, buildingMapper);
    }

    @Test
    void getBuildingDetailsById_shouldThrowWhenNotFound() {
        Broker broker = aBrokerWithId(1L);

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findDetailsById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.getBuildingDetailsById(1L, EMAIL));

        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findDetailsById(1L);
        verifyNoInteractions(buildingMapper, clientRepository, cityRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateBuilding_shouldUpdateUsingMapper_andReturnResponse() {
        Broker broker = aBrokerWithId(1L);

        BuildingUpdateRequestDTO dto = aBuildingUpdateRequest();
        City city = aCityWithId(dto.cityId());

        Building building = aBuilding();
        Building saved = new Building();
        BuildingDetailsResponseDTO response = aBuildingDetailsResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(dto.cityId())).thenReturn(Optional.of(city));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toDetailsResponse(saved)).thenReturn(response);

        BuildingDetailsResponseDTO result = buildingService.updateBuilding(1L, dto, EMAIL);

        assertSame(response, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(dto.cityId());
        verify(buildingMapper).updateFromDto(dto, city, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toDetailsResponse(saved);

        verifyNoInteractions(clientRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, cityRepository, buildingMapper);
    }

    @Test
    void updateBuilding_shouldThrowWhenCityNotFound() {
        Broker broker = aBrokerWithId(1L);

        BuildingUpdateRequestDTO dto = aBuildingUpdateRequest();
        Building building = aBuilding();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(dto.cityId())).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.updateBuilding(1L, dto, EMAIL));

        assertHasError(ex, ErrorCode.CITY_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(dto.cityId());
        verify(buildingRepository, never()).save(any());

        verifyNoInteractions(buildingMapper, clientRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, cityRepository);
    }

    // ---------------------  PATCH  ---------------------

    @Test
    void patchBuilding_whenCityIdNull_shouldPassNullCityToMapper() {
        Broker broker = aBrokerWithId(1L);

        BuildingPatchDTO dto = aBuildingPatchPartial();
        Building building = aBuilding();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        BuildingResponseDTO result = buildingService.patchBuilding(1L, dto, EMAIL);

        assertSame(response, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(buildingMapper).patchFromDto(dto, null, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoInteractions(cityRepository, clientRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, buildingMapper);
    }

    @Test
    void patchBuilding_whenCityIdProvided_shouldResolveCityAndPassToMapper() {
        Broker broker = aBrokerWithId(1L);

        BuildingPatchDTO dto = aBuildingPatchCityOnly();
        City city = aCityWithId(2L);

        Building building = aBuilding();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        BuildingResponseDTO result = buildingService.patchBuilding(1L, dto, EMAIL);

        assertSame(response, result);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(2L);
        verify(buildingMapper).patchFromDto(dto, city, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoInteractions(clientRepository);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository, cityRepository, buildingMapper);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteBuilding_shouldDelete() {
        Broker broker = aBrokerWithId(1L);
        Building building = aBuilding();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));

        buildingService.deleteBuilding(1L, EMAIL);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(buildingRepository).delete(building);

        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository);
    }

    @Test
    void deleteBuilding_shouldThrowWhenNotFound() {
        Broker broker = aBrokerWithId(1L);

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.deleteBuilding(1L, EMAIL));

        assertHasError(ex, ErrorCode.BUILDING_NOT_FOUND);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(buildingRepository, never()).delete(any());

        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository);
    }

    @Test
    void deleteBuilding_shouldThrowWhenHasDependencies() {
        Broker broker = aBrokerWithId(1L);
        Building building = aBuilding();

        when(currentBrokerProvider.getActiveOrThrow(EMAIL)).thenReturn(broker);
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        doThrow(new DataIntegrityViolationException("fk")).when(buildingRepository).delete(building);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> buildingService.deleteBuilding(1L, EMAIL));

        assertHasError(ex, ErrorCode.BUILDING_HAS_DEPENDENCIES);

        verify(currentBrokerProvider).getActiveOrThrow(EMAIL);
        verify(buildingRepository).findById(1L);
        verify(buildingRepository).delete(building);

        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
        verifyNoMoreInteractions(currentBrokerProvider, buildingRepository);
    }
}

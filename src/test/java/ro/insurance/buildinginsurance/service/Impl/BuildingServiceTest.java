package ro.insurance.buildinginsurance.service.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClient;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock private BuildingRepository buildingRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CityRepository cityRepository;
    @Mock private BuildingMapper buildingMapper;

    @InjectMocks private BuildingService buildingService;

    // ---------------------  CREATE  ---------------------

    @Test
    void registerNewBuilding_shouldMapSaveAndReturnResponse() {
        // Arrange
        BuildingRequestDTO buildingRequestDTO = aBuildingRequest();
        Client client = aClient();
        City city = aCityWithId(buildingRequestDTO.cityId());

        Building building = new Building();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(clientRepository.findById(buildingRequestDTO.clientId())).thenReturn(Optional.of(client));
        when(cityRepository.findById(buildingRequestDTO.cityId())).thenReturn(Optional.of(city));
        when(buildingMapper.toEntity(buildingRequestDTO, client, city)).thenReturn(building);
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        // Act
        BuildingResponseDTO result = buildingService.registerNewBuilding(buildingRequestDTO);

        // Assert
        assertSame(response, result);

        verify(clientRepository).findById(buildingRequestDTO.clientId());
        verify(cityRepository).findById(buildingRequestDTO.cityId());
        verify(buildingMapper).toEntity(buildingRequestDTO, client, city);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);
        verifyNoMoreInteractions(buildingRepository, clientRepository, cityRepository, buildingMapper);
    }

    @Test
    void registerNewBuilding_shouldThrowWhenClientNotFound() {
        // Arrange
        BuildingRequestDTO buildingRequestDTO = aBuildingRequest();
        when(clientRepository.findById(buildingRequestDTO.clientId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.registerNewBuilding(buildingRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(buildingRequestDTO.clientId());
        verifyNoInteractions(cityRepository, buildingRepository, buildingMapper);
    }

    @Test
    void registerNewBuilding_shouldThrowWhenCityNotFound() {
        // Arrange
        BuildingRequestDTO buildingRequestDTO = aBuildingRequest();
        Client client = aClient();

        when(clientRepository.findById(buildingRequestDTO.clientId())).thenReturn(Optional.of(client));
        when(cityRepository.findById(buildingRequestDTO.cityId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.registerNewBuilding(buildingRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(buildingRequestDTO.clientId());
        verify(cityRepository).findById(buildingRequestDTO.cityId());
        verifyNoInteractions(buildingRepository, buildingMapper);
    }

    // ---------------------  GET  ---------------------

    @Test
    void getBuildingById_shouldReturnResponse() {
        // Arrange
        Building building = aBuilding();
        BuildingResponseDTO response = aBuildingResponse();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(buildingMapper.toResponse(building)).thenReturn(response);

        // Act
        BuildingResponseDTO result = buildingService.getBuildingById(1L);

        assertSame(response, result);

        verify(buildingRepository).findById(1L);
        verify(buildingMapper).toResponse(building);
        verifyNoMoreInteractions(buildingRepository, buildingMapper);
        verifyNoInteractions(clientRepository, cityRepository);
    }

    @Test
    void getBuildingById_shouldThrowWhenNotFound() {
        // Arrange
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.getBuildingById(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findById(1L);
        verifyNoInteractions(buildingMapper, clientRepository, cityRepository);
    }

    @Test
    void getBuildingsByClientId_shouldReturnPage() {
        // Arrange
        long clientId = 1L;
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").ascending());

        Client client = aClient();
        Building building = aBuilding();
        Page<Building> page = new PageImpl<>(List.of(building), pageable, 1);

        BuildingResponseDTO response = aBuildingResponse();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(buildingRepository.findByClientId(clientId, pageable)).thenReturn(page);
        when(buildingMapper.toResponse(building)).thenReturn(response);

        // Act
        Page<BuildingResponseDTO> result = buildingService.getBuildingsByClientId(clientId, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().getFirst());

        verify(clientRepository).findById(clientId);
        verify(buildingRepository).findByClientId(clientId, pageable);
        verify(buildingMapper).toResponse(building);
        verifyNoMoreInteractions(clientRepository, buildingRepository, buildingMapper);
        verifyNoInteractions(cityRepository);
    }

    @Test
    void getBuildingsByClientId_shouldThrowWhenClientNotFound() {
        // Arrange
        long clientId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.getBuildingsByClientId(clientId, pageable));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CLIENT_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(clientRepository).findById(clientId);
        verifyNoInteractions(buildingRepository, buildingMapper, cityRepository);
    }

    @Test
    void getBuildingDetailsById_shouldReturnDetailsResponse() {
        // Arrange
        Building building = aBuilding();
        BuildingDetailsResponseDTO details = aBuildingDetailsResponse();

        when(buildingRepository.findDetailsById(1L)).thenReturn(Optional.of(building));
        when(buildingMapper.toDetailsResponse(building)).thenReturn(details);

        // Act
        BuildingDetailsResponseDTO result = buildingService.getBuildingDetailsById(1L);

        // Assert
        assertSame(details, result);

        verify(buildingRepository).findDetailsById(1L);
        verify(buildingMapper).toDetailsResponse(building);
        verifyNoMoreInteractions(buildingRepository, buildingMapper);
        verifyNoInteractions(clientRepository, cityRepository);
    }

    @Test
    void getBuildingDetailsById_shouldThrowWhenNotFound() {
        // Arrange
        when(buildingRepository.findDetailsById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.getBuildingDetailsById(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findDetailsById(1L);
        verifyNoInteractions(buildingMapper, clientRepository, cityRepository);
    }

    // ---------------------  UPDATE  ---------------------

    @Test
    void updateBuilding_shouldUpdateUsingMapper_keepClientUnchanged_andReturnResponse() {
        // Arrange
        BuildingUpdateRequestDTO buildingUpdateRequestDTO = aBuildingUpdateRequest();
        City city = aCityWithId(buildingUpdateRequestDTO.cityId());

        Building building = aBuilding();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(buildingUpdateRequestDTO.cityId())).thenReturn(Optional.of(city));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        // Act
        BuildingResponseDTO result = buildingService.updateBuilding(1L, buildingUpdateRequestDTO);

        // Assert
        assertSame(response, result);

        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(buildingUpdateRequestDTO.cityId());
        verify(buildingMapper).updateFromDto(buildingUpdateRequestDTO, city, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoInteractions(clientRepository);
        verifyNoMoreInteractions(buildingRepository, cityRepository, buildingMapper);
    }

    @Test
    void updateBuilding_shouldThrowWhenCityNotFound() {
        // Arrange
        BuildingUpdateRequestDTO buildingUpdateRequestDTO = aBuildingUpdateRequest();
        Building building = aBuilding();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(buildingUpdateRequestDTO.cityId())).thenReturn(Optional.empty());

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.updateBuilding(1L, buildingUpdateRequestDTO));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(buildingUpdateRequestDTO.cityId());
        verify(buildingRepository, never()).save(any());
        verifyNoInteractions(buildingMapper, clientRepository);
    }

    // ---------------------  PATCH  ---------------------

    @Test
    void patchBuilding_whenCityIdNull_shouldPassNullCityToMapper() {
        // Arrange
        BuildingPatchDTO buildingPatchDTO = aBuildingPatchPartial();
        Building building = aBuilding();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        // Act
        BuildingResponseDTO result = buildingService.patchBuilding(1L, buildingPatchDTO);

        // Assert
        assertSame(response, result);

        verify(buildingRepository).findById(1L);
        verify(buildingMapper).patchFromDto(buildingPatchDTO, null, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoInteractions(cityRepository, clientRepository);
        verifyNoMoreInteractions(buildingRepository, buildingMapper);
    }

    @Test
    void patchBuilding_whenCityIdProvided_shouldResolveCityAndPassToMapper() {
        // Arrange
        BuildingPatchDTO buildingPatchDTO = aBuildingPatchCityOnly();
        City city = aCityWithId(2L);

        Building building = aBuilding();
        Building saved = new Building();
        BuildingResponseDTO response = aBuildingResponse();

        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(city));
        when(buildingRepository.save(building)).thenReturn(saved);
        when(buildingMapper.toResponse(saved)).thenReturn(response);

        // Act
        BuildingResponseDTO result = buildingService.patchBuilding(1L, buildingPatchDTO);

        // Assert
        assertSame(response, result);

        verify(buildingRepository).findById(1L);
        verify(cityRepository).findById(2L);
        verify(buildingMapper).patchFromDto(buildingPatchDTO, city, building);
        verify(buildingRepository).save(building);
        verify(buildingMapper).toResponse(saved);

        verifyNoInteractions(clientRepository);
        verifyNoMoreInteractions(buildingRepository, cityRepository, buildingMapper);
    }

    // ---------------------  DELETE  ---------------------

    @Test
    void deleteBuilding_shouldDelete() {
        // Act
        buildingService.deleteBuilding(1L);

        // Assert
        verify(buildingRepository).deleteById(1L);
        verifyNoMoreInteractions(buildingRepository);
        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
    }

    @Test
    void deleteBuilding_shouldThrowWhenNotFound() {
        // Arrange
        doThrow(new EmptyResultDataAccessException(1)).when(buildingRepository).deleteById(1L);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.deleteBuilding(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).deleteById(1L);
        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
    }

    @Test
    void deleteBuilding_shouldThrowWhenHasDependencies() {
        // Arrange
        doThrow(new DataIntegrityViolationException("fk")).when(buildingRepository).deleteById(1L);

        // Act + Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> buildingService.deleteBuilding(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.BUILDING_HAS_DEPENDENCIES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(buildingRepository).deleteById(1L);
        verifyNoInteractions(clientRepository, cityRepository, buildingMapper);
    }
}

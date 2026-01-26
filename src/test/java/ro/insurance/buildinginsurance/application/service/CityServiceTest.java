package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import ro.insurance.buildinginsurance.application.service.impl.CityService;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CityMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CityRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCounty;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyDTO;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

        @Mock
        private CityRepository cityRepository;
        @Mock
        private CountyRepository countyRepository;
        @Mock
        private CityMapper cityMapper;

        @InjectMocks
        private CityService cityService;

        // ---------------------  CREATE  ---------------------

        @Test
        void registerClient_shouldCreateAndReturnResponse(){
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequest();
                County county = aCounty();
                City city = aCity();
                CityResponseDTO cityResponseDTO = aCityResponse();

                when(cityRepository.existsByNameIgnoreCaseAndCountyId("Adjud", 1L)).thenReturn(false);
                when(countyRepository.findById(1L)).thenReturn(Optional.of(county));
                when(cityMapper.toEntity(cityRequestDTO, county)).thenReturn(city);
                when(cityRepository.save(city)).thenReturn(city);
                when(cityMapper.toResponse(city)).thenReturn(cityResponseDTO);

                // Act
                CityResponseDTO result = cityService.registerCity(cityRequestDTO);

                // Assert

                assertEquals(cityResponseDTO,result);

                verify(cityRepository).existsByNameIgnoreCaseAndCountyId(cityRequestDTO.name(),cityRequestDTO.countyId());
                verify(countyRepository).findById(cityRequestDTO.countyId());
                verify(cityMapper).toEntity(cityRequestDTO,county);
                verify(cityRepository).save(city);
                verify(cityMapper).toResponse(city);
                verifyNoMoreInteractions(cityMapper,cityRepository,countyRepository);
        }

        @Test
        void registerCity_shouldTrimNameBeforeUniquenessCheck() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequestWithName("   Adjud   ");
                County county = aCounty();
                City city = aCity();
                CityResponseDTO response = aCityResponse();

                when(cityRepository.existsByNameIgnoreCaseAndCountyId("Adjud", 1L)).thenReturn(false);
                when(countyRepository.findById(1L)).thenReturn(Optional.of(county));
                when(cityMapper.toEntity(cityRequestDTO, county)).thenReturn(city);
                when(cityRepository.save(city)).thenReturn(city);
                when(cityMapper.toResponse(city)).thenReturn(response);

                // Act
                CityResponseDTO result = cityService.registerCity(cityRequestDTO);

                // Assert
                assertEquals(response, result);

                verify(cityRepository).existsByNameIgnoreCaseAndCountyId("Adjud", 1L);
                verify(countyRepository).findById(1L);
                verify(cityMapper).toEntity(cityRequestDTO, county);
                verify(cityRepository).save(city);
                verify(cityMapper).toResponse(city);
                verifyNoMoreInteractions(cityRepository, countyRepository, cityMapper);
        }

        @Test
        void registerCity_shouldThrowWhenCityAlreadyExists() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequest();

                when(cityRepository.existsByNameIgnoreCaseAndCountyId("Adjud", 1L)).thenReturn(true);

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.registerCity(cityRequestDTO));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_ALREADY_EXIST.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).existsByNameIgnoreCaseAndCountyId("Adjud", 1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(countyRepository, cityMapper);
        }

        @Test
        void registerCity_shouldThrowWhenCountyNotFound() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequest();

                when(cityRepository.existsByNameIgnoreCaseAndCountyId(cityRequestDTO.name(), cityRequestDTO.countyId())).thenReturn(false);
                when(countyRepository.findById(cityRequestDTO.countyId())).thenReturn(Optional.empty());

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.registerCity(cityRequestDTO));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_NOT_FOUND.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).existsByNameIgnoreCaseAndCountyId(cityRequestDTO.name(), cityRequestDTO.countyId());
                verify(countyRepository).findById(cityRequestDTO.countyId());
                verifyNoMoreInteractions(cityRepository, countyRepository);
                verifyNoInteractions(cityMapper);
        }

        // --------------------- GET ---------------------

        @Test
        void getCityById_shouldReturnResponse() {
                // Arrange
                City city = aCity();
                CityResponseDTO response = aCityResponse();

                when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
                when(cityMapper.toResponse(city)).thenReturn(response);

                // Act
                CityResponseDTO result = cityService.getCityById(1L);

                // Assert
                assertEquals(response, result);

                verify(cityRepository).findById(1L);
                verify(cityMapper).toResponse(city);
                verifyNoMoreInteractions(cityRepository, cityMapper);
                verifyNoInteractions(countyRepository);
        }

        @Test
        void getCityById_whenNotFound_shouldThrowBusinessException() {
                // Arrange
                when(cityRepository.findById(1L)).thenReturn(Optional.empty());

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.getCityById(1L));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).findById(1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(cityMapper, countyRepository);
        }

        @Test
        void getCityByPartialName_shouldReturnPagedResponse() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());
                City city1 = aCityWithId(1L);
                City city2 = aCityWithId(2L);

                Page<City> cities = new PageImpl<>(List.of(city1, city2), pageable, 2);

                CityResponseDTO cityResponseDTO1 = aCityResponse();
                CityResponseDTO cityResponseDTO2 = new CityResponseDTO(2L, "Adjud-2", aCountyDTO());

                when(cityRepository.getCityByPartialName("adj", pageable)).thenReturn(cities);
                when(cityMapper.toResponse(city1)).thenReturn(cityResponseDTO1);
                when(cityMapper.toResponse(city2)).thenReturn(cityResponseDTO2);

                // Act
                Page<CityResponseDTO> result = cityService.getCityByPartialName("adj", pageable);

                // Assert
                assertEquals(2, result.getTotalElements());
                assertEquals(2, result.getContent().size());
                assertEquals(cityResponseDTO1, result.getContent().get(0));
                assertEquals(cityResponseDTO2, result.getContent().get(1));

                verify(cityRepository).getCityByPartialName("adj", pageable);
                verify(cityMapper).toResponse(city1);
                verify(cityMapper).toResponse(city2);
                verifyNoMoreInteractions(cityRepository, cityMapper);
                verifyNoInteractions(countyRepository);
        }

        // --------------------- UPDATE ---------------------

        @Test
        void updateCity_shouldUpdateAndReturnResponse() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequestWithNameAndCounty("Adjud Updated", 1L);
                City city = aCity();
                County county = aCounty();
                City saved = aCityWithId(1L);
                CityResponseDTO response = new CityResponseDTO(1L, "Adjud Updated", aCountyDTO());

                when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
                when(countyRepository.findById(1L)).thenReturn(Optional.of(county));
                when(cityRepository.save(city)).thenReturn(saved);
                when(cityMapper.toResponse(saved)).thenReturn(response);

                // Act
                CityResponseDTO result = cityService.updateCity(1L, cityRequestDTO);

                // Assert
                assertEquals(response, result);

                verify(cityRepository).findById(1L);
                verify(countyRepository).findById(1L);
                verify(cityMapper).updateFromDTO(cityRequestDTO, county, city);
                verify(cityRepository).save(city);
                verify(cityMapper).toResponse(saved);
                verifyNoMoreInteractions(cityRepository, countyRepository, cityMapper);
        }

        @Test
        void updateCity_whenCityNotFound_shouldThrowBusinessException() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequest();
                when(cityRepository.findById(1L)).thenReturn(Optional.empty());

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.updateCity(1L, cityRequestDTO));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).findById(1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(countyRepository, cityMapper);
        }

        @Test
        void updateCity_whenCountyNotFound_shouldThrowBusinessException() {
                // Arrange
                CityRequestDTO cityRequestDTO = aCityRequest();
                City city = aCity();

                when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
                when(countyRepository.findById(1L)).thenReturn(Optional.empty());

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.updateCity(1L, cityRequestDTO));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_NOT_FOUND.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).findById(1L);
                verify(countyRepository).findById(1L);
                verifyNoMoreInteractions(cityRepository, countyRepository);
                verifyNoInteractions(cityMapper);
        }

        // --------------------- DELETE ---------------------

        @Test
        void deleteCity_shouldCallRepositoryDelete() {
                // Arrange
                doNothing().when(cityRepository).deleteById(1L);

                // Act + Assert
                assertDoesNotThrow(() -> cityService.deleteCity(1L));

                verify(cityRepository).deleteById(1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(cityMapper, countyRepository);
        }

        @Test
        void deleteCity_whenNotFound_shouldThrowBusinessException() {
                // Arrange
                doThrow(new EmptyResultDataAccessException(1)).when(cityRepository).deleteById(1L);

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.deleteCity(1L));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_NOT_FOUND.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).deleteById(1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(cityMapper, countyRepository);
        }

        @Test
        void deleteCity_whenHasDependencies_shouldThrowBusinessException() {
                // Arrange
                doThrow(new DataIntegrityViolationException("FK")).when(cityRepository).deleteById(1L);

                // Act
                BusinessException ex = assertThrows(BusinessException.class, () -> cityService.deleteCity(1L));

                // Assert
                assertAll(
                        () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.CITY_HAS_DEPENDENCIES.name().equals(e.getCode()))),
                        () -> assertEquals(1, ex.getErrors().size())
                );

                verify(cityRepository).deleteById(1L);
                verifyNoMoreInteractions(cityRepository);
                verifyNoInteractions(cityMapper, countyRepository);
        }


}
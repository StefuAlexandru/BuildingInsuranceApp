package ro.insurance.buildinginsurance.application.service.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;

import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CountyMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class CountyServiceTest {

    @Mock
    private CountyRepository countyRepository;
    @Mock
    private CountyMapper countyMapper;
    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountyService countyService;

    // --------------------- CREATE ---------------------

    @Test
    void registerCounty_shouldCreateAndReturnResponse() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        String trimmed = countyRequestDTO.name().trim();

        Country country = aCountryWithId(countyRequestDTO.countryId());
        County county = aCountyWithId(1L);
        County saved = aCountyWithId(1L);
        CountyResponseDTO response = aCountyResponse();

        when(countyRepository.existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId())).thenReturn(false);
        when(countryRepository.findById(countyRequestDTO.countryId())).thenReturn(Optional.of(country));
        when(countyMapper.toEntity(countyRequestDTO, country)).thenReturn(county);
        when(countyRepository.save(county)).thenReturn(saved);
        when(countyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CountyResponseDTO result = countyService.registerCounty(countyRequestDTO);

        // Assert
        assertEquals(response, result);

        verify(countyRepository).existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId());
        verify(countryRepository).findById(countyRequestDTO.countryId());
        verify(countyMapper).toEntity(countyRequestDTO, country);
        verify(countyRepository).save(county);
        verify(countyMapper).toResponse(saved);
        verifyNoMoreInteractions(countyRepository, countryRepository, countyMapper);
    }

    @Test
    void registerCounty_shouldTrimNameBeforeUniquenessCheck() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequestWithNameAndCountryId("   Vrancea   ", 1L);
        String trimmed = countyRequestDTO.name().trim();

        Country country = aCountryWithId(1L);
        County county = aCountyWithId(1L);
        CountyResponseDTO response = aCountyResponse();

        when(countyRepository.existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId())).thenReturn(false);
        when(countryRepository.findById(countyRequestDTO.countryId())).thenReturn(Optional.of(country));
        when(countyMapper.toEntity(countyRequestDTO, country)).thenReturn(county);
        when(countyRepository.save(county)).thenReturn(county);
        when(countyMapper.toResponse(county)).thenReturn(response);

        // Act
        CountyResponseDTO result = countyService.registerCounty(countyRequestDTO);

        // Assert
        assertEquals(response, result);
        verify(countyRepository).existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId());
        verify(countryRepository).findById(countyRequestDTO.countryId());
        verify(countyMapper).toEntity(countyRequestDTO, country);
        verify(countyRepository).save(county);
        verify(countyMapper).toResponse(county);
        verifyNoMoreInteractions(countyRepository, countryRepository, countyMapper);
    }

    @Test
    void registerCounty_whenAlreadyExists_shouldThrowBusinessException() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        String trimmed = countyRequestDTO.name().trim();

        when(countyRepository.existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId())).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.registerCounty(countyRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_ALREADY_EXIST.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId());
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void registerCounty_whenCountryNotFound_shouldThrowBusinessException() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        String trimmed = countyRequestDTO.name().trim();

        when(countyRepository.existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId())).thenReturn(false);
        when(countryRepository.findById(countyRequestDTO.countryId())).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.registerCounty(countyRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId());
        verify(countryRepository).findById(countyRequestDTO.countryId());
        verifyNoMoreInteractions(countyRepository, countryRepository);
        verifyNoInteractions(countyMapper);
    }

    // --------------------- GET ---------------------

    @Test
    void getCountyById_shouldReturnResponse() {
        // Arrange
        County county = aCountyWithId(1L);
        CountyResponseDTO response = aCountyResponse();

        when(countyRepository.findById(1L)).thenReturn(Optional.of(county));
        when(countyMapper.toResponse(county)).thenReturn(response);

        // Act
        CountyResponseDTO result = countyService.getCountyById(1L);

        // Assert
        assertEquals(response, result);

        verify(countyRepository).findById(1L);
        verify(countyMapper).toResponse(county);
        verifyNoMoreInteractions(countyRepository, countyMapper);
        verifyNoInteractions(countryRepository);
    }

    @Test
    void getCountyById_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        when(countyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.getCountyById(1L));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).findById(1L);
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void getCountyByPartialName_whenNull_shouldUseEmptyString_andReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());

        County c1 = aCountyWithId(1L);
        County c2 = aCountyWithId(2L);

        Page<County> counties = new PageImpl<>(List.of(c1, c2), pageable, 2);

        CountyResponseDTO d1 = aCountyResponse();
        CountyResponseDTO d2 = aCountyResponseWithIdAndName(2L, "Bihor");

        when(countyRepository.getCountyByPartialName("", pageable)).thenReturn(counties);
        when(countyMapper.toResponse(c1)).thenReturn(d1);
        when(countyMapper.toResponse(c2)).thenReturn(d2);

        // Act
        Page<CountyResponseDTO> result = countyService.getCountyByPartialName(null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(d1, result.getContent().get(0));
        assertEquals(d2, result.getContent().get(1));

        verify(countyRepository).getCountyByPartialName("", pageable);
        verify(countyMapper).toResponse(c1);
        verify(countyMapper).toResponse(c2);
        verifyNoMoreInteractions(countyRepository, countyMapper);
        verifyNoInteractions(countryRepository);
    }

    @Test
    void getCountyByPartialName_shouldReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        County county = aCountyWithId(1L);

        Page<County> counties = new PageImpl<>(List.of(county), pageable, 1);
        CountyResponseDTO response = aCountyResponse();

        when(countyRepository.getCountyByPartialName("vra", pageable)).thenReturn(counties);
        when(countyMapper.toResponse(county)).thenReturn(response);

        // Act
        Page<CountyResponseDTO> result = countyService.getCountyByPartialName("vra", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().getFirst());

        verify(countyRepository).getCountyByPartialName("vra", pageable);
        verify(countyMapper).toResponse(county);
        verifyNoMoreInteractions(countyRepository, countyMapper);
        verifyNoInteractions(countryRepository);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateCounty_shouldUpdateAndReturnResponse() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequestWithNameAndCountryId("Iasi", 2L);

        County existing = aCountyWithId(99L);
        Country newCountry = aCountryWithId(2L);
        County saved = aCountyWithId(99L);
        CountyResponseDTO response = aCountyResponse();

        when(countyRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(countyRepository.existsByNameIgnoreCaseAndCountryId("Iasi", 2L)).thenReturn(false);
        when(countryRepository.findById(2L)).thenReturn(Optional.of(newCountry));

        when(countyRepository.save(existing)).thenReturn(saved);
        when(countyMapper.toResponse(saved)).thenReturn(response);

        // Act
        CountyResponseDTO result = countyService.updateCounty(99L, countyRequestDTO);

        // Assert
        assertEquals(response, result);

        verify(countyRepository).findById(99L);
        verify(countyRepository).existsByNameIgnoreCaseAndCountryId("Iasi", 2L);
        verify(countryRepository).findById(2L);
        verify(countyMapper).updateFromDTO(countyRequestDTO, newCountry, existing);
        verify(countyRepository).save(existing);
        verify(countyMapper).toResponse(saved);
        verifyNoMoreInteractions(countyRepository, countryRepository, countyMapper);
    }

    @Test
    void updateCounty_whenCountyNotFound_shouldThrowBusinessException() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        when(countyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.updateCounty(1L, countyRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).findById(1L);
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void updateCounty_whenAlreadyExists_shouldThrowBusinessException() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        String trimmed = countyRequestDTO.name().trim();

        County existing = aCountyWithId(1L);

        when(countyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(countyRepository.existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId())).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.updateCounty(1L, countyRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_ALREADY_EXIST.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).findById(1L);
        verify(countyRepository).existsByNameIgnoreCaseAndCountryId(trimmed, countyRequestDTO.countryId());
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void updateCounty_whenCountryNotFound_shouldThrowBusinessException() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequestWithNameAndCountryId("Iasi", 2L);

        County existing = aCountyWithId(1L);

        when(countyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(countyRepository.existsByNameIgnoreCaseAndCountryId("Iasi", 2L)).thenReturn(false);
        when(countryRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.updateCounty(1L, countyRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).findById(1L);
        verify(countyRepository).existsByNameIgnoreCaseAndCountryId("Iasi", 2L);
        verify(countryRepository).findById(2L);
        verifyNoMoreInteractions(countyRepository, countryRepository);
        verifyNoInteractions(countyMapper);
    }

    // --------------------- DELETE ---------------------

    @Test
    void deleteCounty_shouldCallRepositoryDelete() {
        doNothing().when(countyRepository).deleteById(1L);

        assertDoesNotThrow(() -> countyService.deleteCounty(1L));

        verify(countyRepository).deleteById(1L);
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void deleteCounty_whenNotFound_shouldThrowBusinessException() {
        doThrow(new EmptyResultDataAccessException(1)).when(countyRepository).deleteById(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.deleteCounty(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).deleteById(1L);
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }

    @Test
    void deleteCounty_whenHasDependencies_shouldThrowBusinessException() {
        doThrow(new DataIntegrityViolationException("FK")).when(countyRepository).deleteById(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> countyService.deleteCounty(1L));

        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTY_HAS_DEPENDENCIES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countyRepository).deleteById(1L);
        verifyNoMoreInteractions(countyRepository);
        verifyNoInteractions(countryRepository, countyMapper);
    }
}
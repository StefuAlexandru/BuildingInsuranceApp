package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;

import ro.insurance.buildinginsurance.application.service.impl.CountryService;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;
import ro.insurance.buildinginsurance.application.mapper.CountryMapper;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CountryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CountryMapper countryMapper;

    @InjectMocks
    private CountryService countryService;

    // --------------------- CREATE ---------------------

    @Test
    void registerCountry_shouldCreateAndReturnResponse() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        String trimmedName = countryRequestDTO.name().trim();

        Country country = aCountry();
        Country saved = aCountryWithId(10L);
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(10L, "Romania");

        when(countryRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(false);
        when(countryMapper.toEntity(countryRequestDTO)).thenReturn(country);
        when(countryRepository.save(country)).thenReturn(saved);
        when(countryMapper.toResponse(saved)).thenReturn(countryResponseDTO);

        // Act
        CountryResponseDTO result = countryService.registerCountry(countryRequestDTO);

        // Assert
        assertEquals(countryResponseDTO, result);

        verify(countryRepository).existsByNameIgnoreCase(trimmedName);
        verify(countryMapper).toEntity(countryRequestDTO);
        verify(countryRepository).save(country);
        verify(countryMapper).toResponse(saved);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    @Test
    void registerCountry_shouldTrimNameBeforeUniquenessCheck() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequestWithName("   Romania   ");
        String trimmedName = countryRequestDTO.name().trim();

        Country country = aCountry();
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");

        when(countryRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(false);
        when(countryMapper.toEntity(countryRequestDTO)).thenReturn(country);
        when(countryRepository.save(country)).thenReturn(country);
        when(countryMapper.toResponse(country)).thenReturn(countryResponseDTO);

        // Act
        CountryResponseDTO result = countryService.registerCountry(countryRequestDTO);

        // Assert
        assertEquals(countryResponseDTO, result);

        verify(countryRepository).existsByNameIgnoreCase(trimmedName);
        verify(countryMapper).toEntity(countryRequestDTO);
        verify(countryRepository).save(country);
        verify(countryMapper).toResponse(country);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    @Test
    void registerCountry_whenAlreadyExists_shouldThrowBusinessException() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        String trimmedName = countryRequestDTO.name().trim();

        when(countryRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countryService.registerCountry(countryRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_ALREADY_EXIST.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).existsByNameIgnoreCase(trimmedName);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    // --------------------- GET ---------------------

    @Test
    void getCountryById_shouldReturnResponse() {
        // Arrange
        Country country = aCountryWithId(1L);
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(countryMapper.toResponse(country)).thenReturn(countryResponseDTO);

        // Act
        CountryResponseDTO result = countryService.getCountryById(1L);

        // Assert
        assertEquals(countryResponseDTO, result);

        verify(countryRepository).findById(1L);
        verify(countryMapper).toResponse(country);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    @Test
    void getCountryById_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countryService.getCountryById(1L));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).findById(1L);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    @Test
    void getCountryByPartialName_whenNull_shouldUseEmptyString_andReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());

        Country country1 = aCountryWithId(1L);
        Country country2 = aCountryWithId(2L);

        Page<Country> page = new PageImpl<>(List.of(country1, country2), pageable, 2);

        CountryResponseDTO countryResponseDTO1 = new CountryResponseDTO(1L, "Romania");
        CountryResponseDTO countryResponseDTO2 = new CountryResponseDTO(2L, "Rwanda");

        when(countryRepository.getCountryByPartialName("", pageable)).thenReturn(page);
        when(countryMapper.toResponse(country1)).thenReturn(countryResponseDTO1);
        when(countryMapper.toResponse(country2)).thenReturn(countryResponseDTO2);

        // Act
        Page<CountryResponseDTO> result = countryService.getCountryByPartialName(null, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(countryResponseDTO1, result.getContent().get(0));
        assertEquals(countryResponseDTO2, result.getContent().get(1));

        verify(countryRepository).getCountryByPartialName("", pageable);
        verify(countryMapper).toResponse(country1);
        verify(countryMapper).toResponse(country2);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    @Test
    void getCountryByPartialName_shouldReturnPagedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());

        Country country = aCountryWithId(1L);
        Page<Country> page = new PageImpl<>(List.of(country), pageable, 1);

        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(1L, "Romania");

        when(countryRepository.getCountryByPartialName("rom", pageable)).thenReturn(page);
        when(countryMapper.toResponse(country)).thenReturn(countryResponseDTO);

        // Act
        Page<CountryResponseDTO> result = countryService.getCountryByPartialName("rom", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(countryResponseDTO, result.getContent().getFirst());

        verify(countryRepository).getCountryByPartialName("rom", pageable);
        verify(countryMapper).toResponse(country);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateCountry_shouldUpdateAndReturnResponse() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequestWithName("Rwanda");
        String trimmedName = countryRequestDTO.name().trim();

        Country existing = aCountryWithId(99L);
        Country saved = aCountryWithId(99L);
        CountryResponseDTO countryResponseDTO = new CountryResponseDTO(99L, "Rwanda");

        when(countryRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(countryRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(false);

        when(countryRepository.save(existing)).thenReturn(saved);
        when(countryMapper.toResponse(saved)).thenReturn(countryResponseDTO);

        // Act
        CountryResponseDTO result = countryService.updateCountry(99L, countryRequestDTO);

        // Assert
        assertEquals(countryResponseDTO, result);

        verify(countryRepository).findById(99L);
        verify(countryRepository).existsByNameIgnoreCase(trimmedName);
        verify(countryMapper).updateFromDTO(countryRequestDTO, existing);
        verify(countryRepository).save(existing);
        verify(countryMapper).toResponse(saved);
        verifyNoMoreInteractions(countryRepository, countryMapper);
    }

    @Test
    void updateCountry_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequest();
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(BusinessException.class,
                () -> countryService.updateCountry(1L, countryRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).findById(1L);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    @Test
    void updateCountry_whenAlreadyExists_shouldThrowBusinessException() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequest(); // "Romania"
        String trimmedName = countryRequestDTO.name().trim();

        Country existing = aCountryWithId(1L);

        when(countryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(countryRepository.existsByNameIgnoreCase(trimmedName)).thenReturn(true);

        // Act
        BusinessException ex = assertThrows(BusinessException.class,
                () -> countryService.updateCountry(1L, countryRequestDTO));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_ALREADY_EXIST.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).findById(1L);
        verify(countryRepository).existsByNameIgnoreCase(trimmedName);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    // --------------------- DELETE ---------------------

    @Test
    void deleteCountry_shouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(countryRepository).deleteById(1L);

        // Act + Assert
        assertDoesNotThrow(() -> countryService.deleteCountry(1L));

        verify(countryRepository).deleteById(1L);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    @Test
    void deleteCountry_whenNotFound_shouldThrowBusinessException() {
        // Arrange
        doThrow(new EmptyResultDataAccessException(1)).when(countryRepository).deleteById(1L);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countryService.deleteCountry(1L));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_NOT_FOUND.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).deleteById(1L);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }

    @Test
    void deleteCountry_whenHasDependencies_shouldThrowBusinessException() {
        // Arrange
        doThrow(new DataIntegrityViolationException("FK")).when(countryRepository).deleteById(1L);

        // Act
        BusinessException ex = assertThrows(BusinessException.class, () -> countryService.deleteCountry(1L));

        // Assert
        assertAll(
                () -> assertTrue(ex.getErrors().stream().anyMatch(e -> ErrorCode.COUNTRY_HAS_DEPENDENCIES.name().equals(e.getCode()))),
                () -> assertEquals(1, ex.getErrors().size())
        );

        verify(countryRepository).deleteById(1L);
        verifyNoMoreInteractions(countryRepository);
        verifyNoInteractions(countryMapper);
    }
}

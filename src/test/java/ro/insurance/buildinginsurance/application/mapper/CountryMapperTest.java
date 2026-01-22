package ro.insurance.buildinginsurance.application.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countryDto.CountryResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.Country;

import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.*;

@SpringBootTest
@ActiveProfiles("test")
class CountryMapperTest {

    @Autowired
    private CountryMapper countryMapper;

    // --------------------- CREATE ---------------------

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequest();

        // Act
        Country country = countryMapper.toEntity(countryRequestDTO);

        // Assert
        assertAll(
                () -> assertNotNull(country),
                () -> assertNull(country.getId()),
                () -> assertEquals(countryRequestDTO.name(), country.getName())
        );
    }

    @Test
    void toEntity_shouldTrimName_andTurnBlankIntoNull() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequestWithName("   Romania   ");

        // Act
        Country country = countryMapper.toEntity(countryRequestDTO);

        // Assert
        assertEquals("Romania", country.getName());
    }

    @Test
    void toEntity_whenNameBlank_shouldSetNullName_dueToNormalize() {
        // Arrange
        CountryRequestDTO countryRequestDTO = aCountryRequestWithName("   \t  ");

        // Act
        Country country = countryMapper.toEntity(countryRequestDTO);

        // Assert
        assertNull(country.getName());
    }

    // --------------------- RESPONSE ---------------------

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        Country country = aCountryWithId(10L);

        // Act
        CountryResponseDTO response = countryMapper.toResponse(country);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(country.getId(), response.id()),
                () -> assertEquals(country.getName(), response.name())
        );
    }

    // --------------------- DTO ---------------------

    @Test
    void toDTO_shouldMapFieldsCorrectly() {
        // Arrange
        Country country = aCountryWithId(10L);

        // Act
        CountryDTO countryDTO = countryMapper.toDTO(country);

        // Assert
        assertAll(
                () -> assertNotNull(countryDTO),
                () -> assertEquals(country.getId(), countryDTO.id()),
                () -> assertEquals(country.getName(), countryDTO.name())
        );
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateFromDTO_shouldUpdateName_andKeepId() {
        // Arrange
        Country existing = aCountryWithId(99L);
        existing.setName("OLD");

        CountryRequestDTO update = aCountryRequestWithName("New Country");

        // Act
        countryMapper.updateFromDTO(update, existing);

        // Assert
        assertAll(
                () -> assertEquals(99L, existing.getId()),
                () -> assertEquals("New Country", existing.getName())
        );
    }

    @Test
    void updateFromDTO_shouldTrimName_andTurnBlankIntoNull() {
        // Arrange
        Country existing = aCountryWithId(1L);

        CountryRequestDTO dtoTrim = aCountryRequestWithName("  Romania  ");

        // Act
        countryMapper.updateFromDTO(dtoTrim, existing);

        // Assert
        assertEquals("Romania", existing.getName());

        CountryRequestDTO dtoBlank = aCountryRequestWithName("   \t ");
        countryMapper.updateFromDTO(dtoBlank, existing);
        assertNull(existing.getName());
    }
}

package ro.insurance.buildinginsurance.application.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.countyDto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.entity.Country;

import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.*;

@SpringBootTest
@ActiveProfiles("test")
class CountyMapperTest {

    @Autowired
    private CountyMapper countyMapper;

    // --------------------- CREATE ---------------------

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequest();
        Country country = aCountry();

        // Act
        County county = countyMapper.toEntity(countyRequestDTO, country);

        // Assert
        assertAll(
                () -> assertNotNull(county),
                () -> assertNull(county.getId()),
                () -> assertEquals(countyRequestDTO.name(), county.getName()),
                () -> assertEquals(country, county.getCountry())
        );
    }

    @Test
    void toEntity_shouldTrimName_andTurnBlankIntoNull() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequestWithName("   Vrancea   ");
        Country country = aCountry();

        // Act
        County county = countyMapper.toEntity(countyRequestDTO, country);

        // Assert
        assertEquals("Vrancea", county.getName());
    }

    @Test
    void toEntity_whenNameBlank_shouldSetNullName_dueToNormalize() {
        // Arrange
        CountyRequestDTO countyRequestDTO = aCountyRequestWithName("   \t  ");
        Country country = aCountry();

        // Act
        County county = countyMapper.toEntity(countyRequestDTO, country);

        // Assert
        assertNull(county.getName());
    }

    // --------------------- RESPONSE ---------------------

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        County county = aCountyWithId(10L);

        // Act
        CountyResponseDTO response = countyMapper.toResponse(county);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(county.getId(), response.id()),
                () -> assertEquals(county.getName(), response.name()),
                () -> assertNotNull(response.country()),
                () -> assertEquals(county.getCountry().getId(), response.country().id()),
                () -> assertEquals(county.getCountry().getName(), response.country().name())
        );
    }

    // --------------------- DTO ---------------------

    @Test
    void toDTO_shouldMapCountryIdAndCountryName() {
        // Arrange
        County county = aCountyWithId(10L);

        // Act
        CountyDTO countyDTO = countyMapper.toDTO(county);

        // Assert
        assertAll(
                () -> assertNotNull(countyDTO),
                () -> assertEquals(county.getId(), countyDTO.id()),
                () -> assertEquals(county.getName(), countyDTO.name()),
                () -> assertEquals(county.getCountry().getId(), countyDTO.countryId()),
                () -> assertEquals(county.getCountry().getName(), countyDTO.countryName())
        );
    }

    @Test
    void toDTO_whenCountryNull_shouldReturnNullCountryFields() {
        // Arrange
        County county = aCountyWithId(10L);
        county.setCountry(null);

        // Act
        CountyDTO countyDTO = countyMapper.toDTO(county);

        // Assert
        assertAll(
                () -> assertNotNull(countyDTO),
                () -> assertEquals(county.getId(), countyDTO.id()),
                () -> assertEquals(county.getName(), countyDTO.name()),
                () -> assertNull(countyDTO.countryId()),
                () -> assertNull(countyDTO.countryName())
        );
    }

    // --------------------- UPDATE ---------------------

    @Test
    void updateFromDTO_shouldUpdateNameAndCountry_andKeepId() {
        // Arrange
        County existing = aCountyWithId(99L);
        existing.setName("OLD");

        Country newCountry = aCountryWithId(2L);
        CountyRequestDTO countyRequestDTO = aCountyRequestWithNameAndCountryId("New County", 2L);

        // Act
        countyMapper.updateFromDTO(countyRequestDTO, newCountry, existing);

        // Assert
        assertAll(
                () -> assertEquals(99L, existing.getId()),
                () -> assertEquals("New County", existing.getName()),
                () -> assertEquals(newCountry, existing.getCountry())
        );
    }

    @Test
    void updateFromDTO_shouldTrimName_andTurnBlankIntoNull() {
        // Arrange
        County existing = aCountyWithId(1L);
        Country country = aCountry();

        CountyRequestDTO dtoTrim = aCountyRequestWithNameAndCountryId("  Vrancea  ", 1L);
        countyMapper.updateFromDTO(dtoTrim, country, existing);
        assertEquals("Vrancea", existing.getName());

        CountyRequestDTO dtoBlank = aCountyRequestWithNameAndCountryId("   \t ", 1L);
        countyMapper.updateFromDTO(dtoBlank, country, existing);
        assertNull(existing.getName());
    }
}

package ro.insurance.buildinginsurance.application.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.County;

import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityRequest;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCounty;

@SpringBootTest
@ActiveProfiles("test")
class CityMapperTest {

    @Autowired
    CityMapper cityMapper;

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        // Arrange
        CityRequestDTO dto = aCityRequest();
        County county = aCounty();

        // Act
        City city = cityMapper.toEntity(dto, county);

        // Assert
        assertAll(
                () -> assertNotNull(city),
                () -> assertNull(city.getId()),
                () -> assertEquals(dto.name(), city.getName()),
                () -> assertEquals(county, city.getCounty())
        );
    }

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        City city = aCityWithId(10L);

        // Act
        CityResponseDTO cityResponseDTO = cityMapper.toResponse(city);

        // Assert
        assertAll(
                () -> assertNotNull(cityResponseDTO),
                () -> assertEquals(city.getId(), cityResponseDTO.id()),
                () -> assertEquals(city.getName(), cityResponseDTO.name())
        );
    }

    @Test
    void toDTO_shouldMapNestedCountyAndCountryNames() {
        // Arrange
        City city = aCityWithId(10L);

        // Act
        CityDTO cityDTO = cityMapper.toDTO(city);

        // Assert
        assertAll(
                () -> assertNotNull(cityDTO),
                () -> assertEquals(city.getId(), cityDTO.id()),
                () -> assertEquals(city.getName(), cityDTO.name()),
                () -> assertNotNull(cityDTO.countyName()),
                () -> assertNotNull(cityDTO.countryName()),
                () -> assertEquals(city.getCounty().getName(), cityDTO.countyName()),
                () -> assertEquals(city.getCounty().getCountry().getName(), cityDTO.countryName())
        );
    }

    @Test
    void toDTO_whenCountyNull_shouldNotExplode_andReturnNullNames() {
        // Arrange
        City city = aCityWithId(10L);
        city.setCounty(null);

        // Act
        CityDTO cityDTO = cityMapper.toDTO(city);

        // Assert
        assertAll(
                () -> assertNotNull(cityDTO),
                () -> assertEquals(city.getId(), cityDTO.id()),
                () -> assertEquals(city.getName(), cityDTO.name()),
                () -> assertNull(cityDTO.countyName()),
                () -> assertNull(cityDTO.countryName())
        );
    }

    @Test
    void toDTO_whenCountryNull_shouldReturnNullCountryName() {
        // Arrange
        City city = aCityWithId(10L);
        city.getCounty().setCountry(null);

        // Act
        CityDTO cityDTO = cityMapper.toDTO(city);

        // Assert
        assertAll(
                () -> assertNotNull(cityDTO),
                () -> assertEquals(city.getCounty().getName(), cityDTO.countyName()),
                () -> assertNull(cityDTO.countryName())
        );
    }

    @Test
    void updateFromDTO_shouldUpdateNameAndCounty_andKeepId() {
        // Arrange
        City existing = aCityWithId(99L);
        existing.setName("Old Name");

        County newCounty = aCounty();
        newCounty.setId(10L);

        CityRequestDTO dto = new CityRequestDTO("New City", 10L);

        // Act
        cityMapper.updateFromDTO(dto, newCounty, existing);

        // Assert
        assertAll(
                () -> assertNotNull(existing),
                () -> assertEquals(99L, existing.getId()),
                () -> assertEquals("New City", existing.getName()),
                () -> assertEquals(newCounty, existing.getCounty())
        );
    }

    @Test
    void updateFromDTO_shouldTrimName() {
        // Arrange
        City existing = aCityWithId(1L);
        County county = aCounty();

        CityRequestDTO dto = new CityRequestDTO("   Adjud   ", county.getId());

        // Act
        cityMapper.updateFromDTO(dto, county, existing);

        // Assert
        assertEquals("Adjud", existing.getName());
    }

    @Test
    void updateFromDTO_whenNameBlank_shouldTurnIntoNull() {
        // Arrange
        City existing = aCityWithId(1L);
        existing.setName("Something");
        County county = aCounty();

        CityRequestDTO dto = new CityRequestDTO("   \t  ", county.getId());

        // Act
        cityMapper.updateFromDTO(dto, county, existing);

        // Assert
        assertNull(existing.getName());
    }

    @Test
    void updateFromDTO_whenNameNull_shouldKeepNull() {
        // Arrange
        City existing = aCityWithId(1L);
        County county = aCounty();

        CityRequestDTO dto = new CityRequestDTO(null, county.getId());

        // Act
        cityMapper.updateFromDTO(dto, county, existing);

        // Assert
        assertNull(existing.getName());
    }

}
package ro.insurance.buildinginsurance.application.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingPatchDTO;
import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingRequestDTO;
import ro.insurance.buildinginsurance.web.dto.building_dto.BuildingUpdateRequestDTO;
import ro.insurance.buildinginsurance.domain.entity.Building;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClient;

@SpringBootTest
@ActiveProfiles("test")
class BuildingMapperTest {

    @Autowired
    private BuildingMapper buildingMapper;

    @Test
    void toEntity_shouldMapFieldsCorrectly_andIgnoreId() {
        // Arrange
        BuildingRequestDTO dto = aBuildingRequest();
        Client client = aClient();
        City city = aCity();

        // Act
        Building building = buildingMapper.toEntity(dto, client, city);

        // Assert
        assertAll(
                () -> assertNotNull(building),
                () -> assertNull(building.getId()),
                () -> assertSame(client, building.getClient()),
                () -> assertSame(city, building.getCity()),
                () -> assertEquals(dto.street(), building.getStreet()),
                () -> assertEquals(dto.number(), building.getNumber()),
                () -> assertEquals(dto.constructionYear(), building.getConstructionYear()),
                () -> assertEquals(dto.buildingType(), building.getBuildingType()),
                () -> assertEquals(dto.floors(), building.getFloors()),
                () -> assertEquals(dto.surfaceArea(), building.getSurfaceArea()),
                () -> assertEquals(dto.insuredValue(), building.getInsuredValue()),
                () -> assertEquals(dto.floodZone(), building.getFloodZone()),
                () -> assertEquals(dto.earthquakeZone(), building.getEarthquakeZone())
        );
    }

    @Test
    void updateFromDto_shouldUpdateAllowedFields_keepClient_andTrimStreetNumber() {
        // Arrange
        Building building = aBuilding();
        Client oldClient = building.getClient();

        BuildingUpdateRequestDTO dto = aBuildingUpdateRequest();
        City newCity = aCityWithId(dto.cityId());

        // Act
        buildingMapper.updateFromDto(dto, newCity, building);

        // Assert
        assertAll(
                () -> assertNull(building.getId()),
                () -> assertSame(oldClient, building.getClient()),
                () -> assertSame(newCity, building.getCity()),
                () -> assertEquals("New Street", building.getStreet()),
                () -> assertEquals("11B", building.getNumber()),
                () -> assertEquals(2001, building.getConstructionYear()),
                () -> assertEquals(dto.buildingType(), building.getBuildingType()),
                () -> assertEquals(dto.floors(), building.getFloors()),
                () -> assertEquals(new BigDecimal("130.00"), building.getSurfaceArea()),
                () -> assertEquals(new BigDecimal("60000"), building.getInsuredValue()),
                () -> assertTrue(building.getFloodZone()),
                () -> assertFalse(building.getEarthquakeZone())
        );
    }

    @Test
    void patchFromDto_shouldUpdateOnlyProvidedFields_ignoreNulls_keepClient_keepCityWhenCityParamNull() {
        // Arrange
        Building building = aBuilding();
        Client oldClient = building.getClient();
        City oldCity = building.getCity();

        BuildingPatchDTO patch = aBuildingPatchPartial();

        // Act
        buildingMapper.patchFromDto(patch, null, building);

        // Assert
        assertAll(
                () -> assertNull(building.getId()),
                () -> assertSame(oldClient, building.getClient()),
                () -> assertSame(oldCity, building.getCity()),
                () -> assertEquals("Patched Street", building.getStreet()),
                () -> assertEquals("1", building.getNumber()),
                () -> assertEquals(1990, building.getConstructionYear()),
                () -> assertEquals(5, building.getFloors()),
                () -> assertEquals(new BigDecimal("10.00"), building.getSurfaceArea()),
                () -> assertEquals(new BigDecimal("1000.00"), building.getInsuredValue()),
                () -> assertFalse(building.getFloodZone()),
                () -> assertTrue(building.getEarthquakeZone())
        );
    }

    @Test
    void patchFromDto_whenStreetOrNumberBlank_shouldNotOverwriteExistingValues() {
        // Arrange
        Building building = aBuilding();
        BuildingPatchDTO patch = aBuildingPatchBlankStreetAndNumber();

        // Act
        buildingMapper.patchFromDto(patch, null, building);

        // Assert
        assertAll(
                () -> assertEquals("Old Street", building.getStreet()),
                () -> assertEquals("1", building.getNumber())
        );
    }

    @Test
    void patchFromDto_whenCityProvided_shouldUpdateCityOnly() {
        // Arrange
        Building building = aBuilding();
        City newCity = aCityWithId(2L);
        BuildingPatchDTO patch = aBuildingPatchCityOnly();

        // Act
        buildingMapper.patchFromDto(patch, newCity, building);

        // Assert
        assertSame(newCity, building.getCity());
    }
}

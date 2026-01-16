package ro.insurance.buildinginsurance.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.*;

@SpringBootTest
class ClientMapperTest {

    @Autowired
    private ClientMapper clientMapper;

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        // Arrange
        ClientRequestDTO dto = aClientRequest();
        City city = aCity();

        // Act
        Client client = clientMapper.toEntity(dto, city);

        // Assert
        assertAll("Client entity mapping from ClientRequestDTO and City",
                () -> assertNotNull(client),
                () -> assertNull(client.getId()),

                () -> assertEquals(dto.name(), client.getName()),
                () -> assertEquals(dto.identificationNumber(), client.getIdentificationNumber()),
                () -> assertEquals(dto.email(), client.getEmail()),
                () -> assertEquals(dto.phone(), client.getPhone()),

                () -> assertEquals(ClientType.valueOf(dto.clientType()), client.getClientType()),

                () -> assertEquals(city, client.getAddressCity()),
                () -> assertEquals(dto.addressStreet(), client.getAddressStreet()),
                () -> assertEquals(dto.addressNumber(), client.getAddressNumber()),

                () -> assertNotNull(client.getBuildings()),
                () -> assertTrue(client.getBuildings().isEmpty())
        );
    }

    @Test
    void toEntity_shouldTrimNameEmailPhone() {
        // Arrange
        ClientRequestDTO dto = new ClientRequestDTO(
                "  John  ",
                "INDIVIDUAL",
                "1960101123456",
                "  john@test.com  ",
                " 0712345678 ",
                1L,
                "  Str. Test  ",
                "  10A "
        );
        City city = aCity();

        // Act
        Client client = clientMapper.toEntity(dto, city);

        // Assert
        assertAll(
                () -> assertEquals("John", client.getName()),
                () -> assertEquals("john@test.com", client.getEmail()),
                () -> assertEquals("0712345678", client.getPhone()),
                () -> assertEquals("Str. Test", client.getAddressStreet()),
                () -> assertEquals("10A", client.getAddressNumber())
        );
    }

    @Test
    void toEntity_whenStreetAndNumberNull_shouldKeepNull() {
        // Assert
        ClientRequestDTO dto = new ClientRequestDTO(
                "John",
                "INDIVIDUAL",
                "1960101123456",
                "john@test.com",
                "0712345678",
                1L,
                null,
                null
        );

        // Act
        Client client = clientMapper.toEntity(dto, aCity());

        // Arrange
        assertAll(
                () -> assertNull(client.getAddressStreet()),
                () -> assertNull(client.getAddressNumber())
        );
    }



    @Test
    void updateFromDto_shouldUpdateOnlyAllowedFields() {
        // Arrange
        ClientUpdateRequestDTO dto = aClientUpdateRequest();
        City newCity = aCityWithId(dto.addressCityId());
        Client client = aClient();

        var originalBuildings = client.getBuildings();

        // Act
        clientMapper.updateFromDto(dto, newCity, client);

        // Assert
        assertAll(
                () -> assertEquals(dto.name(), client.getName()),
                () -> assertEquals(dto.email(), client.getEmail()),
                () -> assertEquals(dto.phone(), client.getPhone()),
                () -> assertEquals(dto.addressStreet(), client.getAddressStreet()),
                () -> assertEquals(dto.addressNumber(), client.getAddressNumber()),
                () -> assertEquals(newCity, client.getAddressCity()),

                () -> assertEquals(99L, client.getId()),
                () -> assertEquals("OLD-CNP", client.getIdentificationNumber()),
                () -> assertEquals(ClientType.COMPANY, client.getClientType()),

                () -> assertSame(originalBuildings, client.getBuildings())
        );

    }

    @Test
    void updateFromDto_shouldTurnBlankStreetAndNumberIntoNull() {
        ClientUpdateRequestDTO dto = new ClientUpdateRequestDTO(
                "  Vasi  ",
                "  vasi@test.com  ",
                " 0712345679 ",
                2L,
                "   ",
                "\t "
        );

        City newCity = aCityWithId(dto.addressCityId());
        Client client = aClient();

        clientMapper.updateFromDto(dto, newCity, client);

        assertAll(
                () -> assertEquals("Vasi", client.getName()),
                () -> assertEquals("vasi@test.com", client.getEmail()),
                () -> assertEquals("0712345679", client.getPhone()),
                () -> assertNull(client.getAddressStreet()),
                () -> assertNull(client.getAddressNumber())
        );
    }

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        Client client = aClient();

        // Act
        ClientResponseDTO dto = clientMapper.toResponse(client);

        // Assert
        assertAll("ClientResponseDTO mapping",
                () -> assertNotNull(dto),
                () -> assertEquals(client.getId(), dto.id()),
                () -> assertEquals(client.getClientType(), dto.clientType()),
                () -> assertEquals(client.getName(), dto.name()),
                () -> assertEquals(client.getIdentificationNumber(), dto.identificationNumber()),
                () -> assertEquals(client.getEmail(), dto.email()),
                () -> assertEquals(client.getPhone(), dto.phone()),
                () -> assertEquals(client.getAddressStreet(), dto.addressStreet()),
                () -> assertEquals(client.getAddressNumber(), dto.addressNumber()),

                () -> assertNotNull(dto.city()),
                () -> assertEquals(client.getAddressCity().getId(), dto.city().id()),
                () -> assertEquals(client.getAddressCity().getName(), dto.city().name()),
                () -> assertEquals(client.getAddressCity().getCounty().getName(), dto.city().countyName()),
                () -> assertEquals(client.getAddressCity().getCounty().getCountry().getName(), dto.city().countryName())
        );
    }


    @Test
    void toResponse_whenAddressCityIsNull_shouldReturnNullCity() {
        // Arrange
        Client client = aClient();
        client.setAddressCity(null);

        // Act
        ClientResponseDTO dto = clientMapper.toResponse(client);

        // Assert
        assertAll(
                () -> assertNotNull(dto),
                () -> assertNull(dto.city())
        );

    }



}
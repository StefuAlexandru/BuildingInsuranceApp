package ro.insurance.buildinginsurance.application.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientDetailsResponseDTO;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.web.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.ClientType;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static org.junit.jupiter.api.Assertions.*;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityWithId;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.*;

@SpringBootTest
@ActiveProfiles("test")
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
        assertAll(
                () -> assertNotNull(client),
                () -> assertNull(client.getId()),

                () -> assertEquals(dto.name(), client.getName()),
                () -> assertEquals(dto.identificationNumber(), client.getIdentificationNumber()),
                () -> assertEquals(dto.email(), client.getEmail()),
                () -> assertEquals(dto.phone(), client.getPhone()),

                () -> assertEquals(dto.clientType(), client.getClientType()),

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
        ClientRequestDTO dto = aClientRequest();
        City city = aCity();

        // Act
        Client client = clientMapper.toEntity(dto, city);

        // Assert
        assertAll(
                () -> assertEquals("Gheo", client.getName()),
                () -> assertEquals("gheo@test.com", client.getEmail()),
                () -> assertEquals("0712345678", client.getPhone()),
                () -> assertEquals("Str. Gheo", client.getAddressStreet()),
                () -> assertEquals("10A", client.getAddressNumber())
        );
    }

    @Test
    void toEntity_whenStreetAndNumberNull_shouldKeepNull() {
        // Assert
        ClientRequestDTO clientRequestDTO =aClientRequestWithAddress(1L,null,null);

        // Act
        Client client = clientMapper.toEntity(clientRequestDTO, aCity());

        // Arrange
        assertAll(
                () -> assertNull(client.getAddressStreet()),
                () -> assertNull(client.getAddressNumber())
        );
    }



    @Test
    void updateFromDto_shouldUpdateOnlyAllowedFields() {
        // Arrange
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequest();
        City newCity = aCityWithId(clientUpdateRequestDTO.addressCityId());
        Client client = aClient();

        var originalBuildings = client.getBuildings();

        // Act
        clientMapper.updateFromDto(clientUpdateRequestDTO, newCity, client);

        // Assert
        assertAll(
                () -> assertEquals(clientUpdateRequestDTO.name(), client.getName()),
                () -> assertEquals(clientUpdateRequestDTO.email(), client.getEmail()),
                () -> assertEquals(clientUpdateRequestDTO.phone(), client.getPhone()),
                () -> assertEquals(clientUpdateRequestDTO.addressStreet(), client.getAddressStreet()),
                () -> assertEquals(clientUpdateRequestDTO.addressNumber(), client.getAddressNumber()),
                () -> assertEquals(newCity, client.getAddressCity()),

                () -> assertEquals(99L, client.getId()),
                () -> assertEquals("OLD-CNP", client.getIdentificationNumber()),
                () -> assertEquals(ClientType.COMPANY, client.getClientType()),

                () -> assertSame(originalBuildings, client.getBuildings())
        );

    }

    @Test
    void updateFromDto_shouldTurnBlankStreetAndNumberIntoNull() {
        ClientUpdateRequestDTO clientUpdateRequestDTO = new ClientUpdateRequestDTO(
                "  Vasi  ",
                "  vasi@test.com  ",
                " 0712345679 ",
                2L,
                "   ",
                "\t "
        );

        City newCity = aCityWithId(clientUpdateRequestDTO.addressCityId());
        Client client = aClient();

        clientMapper.updateFromDto(clientUpdateRequestDTO, newCity, client);

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
        ClientResponseDTO clientResponseDTO = clientMapper.toResponse(client);

        // Assert
        assertAll(
                () -> assertNotNull(clientResponseDTO),
                () -> assertEquals(client.getId(), clientResponseDTO.id()),
                () -> assertEquals(client.getClientType(), clientResponseDTO.clientType()),
                () -> assertEquals(client.getName(), clientResponseDTO.name()),
                () -> assertEquals(client.getIdentificationNumber(), clientResponseDTO.identificationNumber()),
                () -> assertEquals(client.getEmail(), clientResponseDTO.email()),
                () -> assertEquals(client.getPhone(), clientResponseDTO.phone()),
                () -> assertEquals(client.getAddressStreet(), clientResponseDTO.addressStreet()),
                () -> assertEquals(client.getAddressNumber(), clientResponseDTO.addressNumber()),

                () -> assertNotNull(clientResponseDTO.city()),
                () -> assertEquals(client.getAddressCity().getId(), clientResponseDTO.city().id()),
                () -> assertEquals(client.getAddressCity().getName(), clientResponseDTO.city().name()),
                () -> assertEquals(client.getAddressCity().getCounty().getName(), clientResponseDTO.city().countyName()),
                () -> assertEquals(client.getAddressCity().getCounty().getCountry().getName(), clientResponseDTO.city().countryName())
        );
    }


    @Test
    void toResponse_whenAddressCityIsNull_shouldReturnNullCity() {
        // Arrange
        Client client = aClient();
        client.setAddressCity(null);

        // Act
        ClientResponseDTO clientResponseDTO = clientMapper.toResponse(client);

        // Assert
        assertAll(
                () -> assertNotNull(clientResponseDTO),
                () -> assertNull(clientResponseDTO.city())
        );
    }

    @Test
    void toDetailsResponse_shouldMapBuildings() {
        // Arrange
        Client client = aClient();
        client.getBuildings().add(aBuildingWithId(1L));
        client.getBuildings().add(aBuildingWithId(2L));

        // Act
        ClientDetailsResponseDTO clientDetailsResponseDTO = clientMapper.toDetailsResponse(client);

        // Assert
        assertAll(
                () -> assertNotNull(clientDetailsResponseDTO),
                () -> assertEquals(client.getId(), clientDetailsResponseDTO.id()),
                () -> assertNotNull(clientDetailsResponseDTO.buildings()),
                () -> assertEquals(2, clientDetailsResponseDTO.buildings().size()),
                () -> assertEquals(1L, clientDetailsResponseDTO.buildings().getFirst().id()),
                () -> assertEquals(2L, clientDetailsResponseDTO.buildings().get(1).id())
        );
    }

    @Test
    void toDetailsResponse_whenNoBuildings_shouldReturnEmptyList() {
        Client client = aClient();
        client.getBuildings().clear();

        ClientDetailsResponseDTO clientDetailsResponseDTO = clientMapper.toDetailsResponse(client);

        assertNotNull(clientDetailsResponseDTO.buildings());
        assertTrue(clientDetailsResponseDTO.buildings().isEmpty());
    }





}
package ro.insurance.buildinginsurance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.dto.clientDto.*;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingDTO;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityDTO;

public class ClientTestDataFactory {
    private ClientTestDataFactory() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // -------------------  BUILDERS  -------------------

    public static String createClientRequestBody(String name, ClientType clientType, String identificationNumber, String email, String phone, Long addressCityId, String addressStreet, String addressNumber) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("clientType", clientType);
        body.put("identificationNumber", identificationNumber);
        body.put("email", email);
        body.put("phone", phone);
        body.put("addressCityId", addressCityId);
        body.put("addressStreet", addressStreet);
        body.put("addressNumber", addressNumber);
        return objectMapper.writeValueAsString(body);
    }

    public static String createClientUpdateRequestBody(String name, String email, String phone, Long addressCityId, String addressStreet, String addressNumber) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("phone", phone);
        body.put("addressCityId", addressCityId);
        body.put("addressStreet", addressStreet);
        body.put("addressNumber", addressNumber);
        return objectMapper.writeValueAsString(body);
    }

    public static String createPatchIdentityBody(String newClientType, String newIdentificationNumber, String reason) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("newClientType", newClientType);
        body.put("newIdentificationNumber", newIdentificationNumber);
        body.put("reason", reason);
        return objectMapper.writeValueAsString(body);
    }

    // -------------------  DTOS  -------------------

    public static ClientDTO aClientDTO(){
        return new ClientDTO(1L,"Gheo");
    }

    public static ClientRequestDTO aClientRequest(){
        return new ClientRequestDTO(
                "Gheo",
                ClientType.INDIVIDUAL,
                "1960101123456",
                "gheo@test.com",
                "0712345678",
                1L,
                "Str. Gheo",
                "10A"
        );
    }

    public static ClientRequestDTO aClientRequestWithAddress(Long cityId, String street, String number) {
        return new ClientRequestDTO(
                "Gheo",
                ClientType.INDIVIDUAL,
                "1960101123456",
                "gheo@test.com",
                "0712345678",
                cityId, street, number
        );
    }



    public static ClientUpdateRequestDTO aClientUpdateRequest(){
        return new ClientUpdateRequestDTO(
                "Vasi",
                "vasi@test.com",
                "0712345679",
                2L,
                "Str. Vasi",
                "20B"
        );
    }

    public static ClientUpdateRequestDTO aClientUpdateRequestWithAddress(Long cityId, String street, String number){
        return new ClientUpdateRequestDTO(
                "Vasi",
                "vasi@test.com",
                "0712345679",
                cityId, street, number
        );
    }

    public static ClientResponseDTO aClientResponse() {
        return new ClientResponseDTO(
                1L,
                ClientType.INDIVIDUAL,
                "Gheo",
                "1960101123456",
                "gheo@test.com",
                "0712345678",
                aCityDTO(),
                "Str. Gheo",
                "10A"
        );
    }

    public static ClientIdentityPatchDTO aClientIdentityPatchDTO(){
        return new ClientIdentityPatchDTO(
                ClientType.INDIVIDUAL,
                "1960101123456",
                "fix"
        );
    }

    public static ClientIdentityPatchDTO aClientIdentityPatchDTOCustom(ClientType clientType,String identificationNumber,String reason){
         return new ClientIdentityPatchDTO(clientType,identificationNumber,reason);
    }

    public static ClientDetailsResponseDTO aClientDetailsResponse() {
        BuildingDTO b = aBuildingDTO();

        return new ClientDetailsResponseDTO(
                1L,
                ClientType.INDIVIDUAL,
                "Gheo",
                "1960101123456",
                "gheo@test.com",
                "0712345678",
                aCityDTO(),
                "Street",
                "10A",
                List.of(b)
        );
    }


    // -------------------  ENTITY  -------------------

    public static Client aClient(){
        Client client = new Client();
        client.setId(99L);
        client.setName("Old Name");
        client.setIdentificationNumber("OLD-CNP");
        client.setEmail("old@test.com");
        client.setPhone("0700000000");
        client.setClientType(ClientType.COMPANY);
        client.setAddressStreet("Old Street");
        client.setAddressNumber("1");
        client.setAddressCity(aCity());
        client.setBuildings(new ArrayList<>());
        return client;
    }

    public static Client aClientIndividual() {
        Client client = new Client();
        client.setId(1L);
        client.setClientType(ClientType.INDIVIDUAL);
        client.setIdentificationNumber("ID1");
        return client;
    }




}

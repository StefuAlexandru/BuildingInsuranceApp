package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;

import java.util.ArrayList;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;

public class ClientTestDataFactory {
    private ClientTestDataFactory() {}

    // -------------------  DTOS  -------------------

    public static ClientRequestDTO aClientRequest(){
        return new ClientRequestDTO(
                "Gheo",
                "INDIVIDUAL",
                "1960101123456",
                "gheo@test.com",
                "0712345678",
                1L,
                "Str. Gheo",
                "10A"
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

}

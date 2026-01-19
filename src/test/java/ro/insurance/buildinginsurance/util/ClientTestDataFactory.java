package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.dto.clientDto.*;
import ro.insurance.buildinginsurance.entity.Client;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.facade.ClientIdentityFacade;

import java.util.ArrayList;
import java.util.List;

import static ro.insurance.buildinginsurance.util.BuildingTestDataFactory.aBuildingDTO;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCity;
import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityDTO;

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

    public static ClientRequestDTO aClientRequestWithAddress(Long cityId, String street, String number) {
        return new ClientRequestDTO(
                "Gheo",
                "INDIVIDUAL",
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

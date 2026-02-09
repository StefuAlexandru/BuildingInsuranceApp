package ro.insurance.buildinginsurance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.domain.entity.City;
import ro.insurance.buildinginsurance.domain.entity.Client;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.entity.Building;
import ro.insurance.buildinginsurance.web.dto.building_dto.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClient;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.aClientDTO;

public class BuildingTestDataFactory {

    private BuildingTestDataFactory() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // -------------------  BUILDERS  -------------------

    public static String createBuildingRequestBody(Long clientId, Long cityId, String street, String number,
                                             Integer constructionYear, String buildingType, Integer floors,
                                             double surfaceArea, double insuredValue, Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", clientId);
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    public static String createBuildingUpdateRequestBody(Long cityId, String street, String number,
                                                   Integer constructionYear, String buildingType, Integer floors,
                                                   double surfaceArea, double insuredValue, Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    public static String createBuildingPatchBody(Long cityId, String street, String number, Integer constructionYear,
                                           String buildingType, Integer floors, Double surfaceArea, Double insuredValue,
                                           Boolean floodZone, Boolean earthquakeZone) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cityId", cityId);
        body.put("street", street);
        body.put("number", number);
        body.put("constructionYear", constructionYear);
        body.put("buildingType", buildingType);
        body.put("floors", floors);
        body.put("surfaceArea", surfaceArea);
        body.put("insuredValue", insuredValue);
        body.put("floodZone", floodZone);
        body.put("earthquakeZone", earthquakeZone);
        return objectMapper.writeValueAsString(body);
    }

    // -------------------  DTOS  -------------------

    public static BuildingDTO aBuildingDTO(){
        return new BuildingDTO(
                100L, aCityDTO(), "Street", "10A", 2000, BuildingType.OFFICE, 2,
                new BigDecimal("120.50"), new BigDecimal("50000"), false, true
        );
    }

    public static BuildingResponseDTO aBuildingResponse() {
        return new BuildingResponseDTO(
                1L, aCityDTO(), "Street", "10A",
                2000, BuildingType.OFFICE
        );
    }

    public static BuildingRequestDTO aBuildingRequest(){
        return new  BuildingRequestDTO(
                1L,2L,"Street","10A",2000,BuildingType.OFFICE,2,
                new BigDecimal("120.50"),new BigDecimal("50000"),false,true
        );
    }

    public static BuildingDetailsResponseDTO aBuildingDetailsResponse() {
        return new BuildingDetailsResponseDTO(
                1L, aClientDTO(), aCityDTO(), "Street", "10A", 2000, BuildingType.OFFICE, 2,
                new BigDecimal("120.50"), new BigDecimal("50000"), false, true
        );
    }

    public static BuildingUpdateRequestDTO aBuildingUpdateRequest() {
        return new BuildingUpdateRequestDTO(
                2L, "New Street", "11B", 2001, BuildingType.RESIDENTIAL, 3,
                new BigDecimal("130.00"), new BigDecimal("60000"), true, false
        );
    }

    public static BuildingPatchDTO aBuildingPatchPartial() {
        return new BuildingPatchDTO(
                null, "  Patched Street  ", null, null, null, 5,
                null, null, null, true
        );
    }

    public static BuildingPatchDTO aBuildingPatchBlankStreetAndNumber() {
        return new BuildingPatchDTO(
                null, "   ", "\t ", null, null, null,
                null, null, null, null
        );
    }

    public static BuildingPatchDTO aBuildingPatchCityOnly() {
        return new BuildingPatchDTO(
                2L, null, null, null, null, null,
                null, null, null, null
        );
    }

    // -------------------  ENTITY  -------------------

    public static Building aBuilding() {
        Client client = aClient();
        City city = aCityWithId(1L);

        Building b = new Building();
        b.setClient(client);
        b.setCity(city);

        b.setStreet("Old Street");
        b.setNumber("1");
        b.setConstructionYear(1990);
        b.setBuildingType(BuildingType.OFFICE);
        b.setFloors(1);
        b.setSurfaceArea(new BigDecimal("10.00"));
        b.setInsuredValue(new BigDecimal("1000.00"));
        b.setFloodZone(false);
        b.setEarthquakeZone(false);

        return b;
    }

    public static Building aBuildingWithId(Long id) {
        Building b = new Building();
        b.setId(id);
        b.setClient(aClient());
        b.setCity(aCityWithId(1L));
        b.setStreet("Street " + id);
        b.setNumber("10A");
        b.setConstructionYear(2000);
        b.setBuildingType(BuildingType.OFFICE);
        b.setFloors(2);
        b.setSurfaceArea(new BigDecimal("120.50"));
        b.setInsuredValue(new BigDecimal("50000"));
        b.setFloodZone(false);
        b.setEarthquakeZone(true);

        return b;
    }
}

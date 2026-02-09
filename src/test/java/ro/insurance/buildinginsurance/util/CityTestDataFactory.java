package ro.insurance.buildinginsurance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityRequestDTO;
import ro.insurance.buildinginsurance.web.dto.city_dto.CityResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.City;

import java.util.LinkedHashMap;
import java.util.Map;

import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCounty;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyDTO;

public class CityTestDataFactory {

    private CityTestDataFactory() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // -------------------  BUILDERS  -------------------

    public static String createCityRequestBody(String name, Long countyId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("countyId", countyId);
        return objectMapper.writeValueAsString(body);
    }

    // -------------------  DTOS  -------------------

    public static CityRequestDTO aCityRequest() {
        return new CityRequestDTO("Adjud", 1L);
    }

    public static CityDTO aCityDTO(){

        return new CityDTO(1L, "Adjud", "Vrancea", "Romania");
    }

    public static CityResponseDTO aCityResponse(){
        return new CityResponseDTO(1L,"Adjud",aCountyDTO());
    }

    public static CityRequestDTO aCityRequestWithName(String name) {
        return new CityRequestDTO(name, 1L);
    }

    public static CityRequestDTO aCityRequestWithNameAndCounty(String name, Long countyId) {
        return new CityRequestDTO(name, countyId);
    }


    // -------------------  ENTITY  -------------------

    public static City aCity() {
        return aCityWithId(1L);
    }

    public static City aCityWithId(Long id) {
        City city = new City();
        city.setId(id);
        city.setName("Adjud");
        city.setCounty(aCounty());
        return city;
    }

    public static City aCityWithoutId() {
        City city = new City();
        city.setName("Adjud");
        city.setCounty(aCounty());
        return city;
    }
}

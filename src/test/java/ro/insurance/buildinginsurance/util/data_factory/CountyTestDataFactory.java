package ro.insurance.buildinginsurance.util.data_factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.County;

import java.util.LinkedHashMap;
import java.util.Map;

import static ro.insurance.buildinginsurance.util.data_factory.CountryTestDataFactory.aCountry;
import static ro.insurance.buildinginsurance.util.data_factory.CountryTestDataFactory.aCountryDTO;

public class CountyTestDataFactory {

    private CountyTestDataFactory() {}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // -------------------  BUILDERS  -------------------

    public static String createCountyRequestBody(String name, Long countryId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("countryId", countryId);
        return objectMapper.writeValueAsString(body);
    }

    // -------------------  DTOS  -------------------

    public static CountyDTO aCountyDTO(){
        return new CountyDTO(1L,"Vrancea",1L,"Romania");
    }

    public static CountyRequestDTO aCountyRequest() {
        return new CountyRequestDTO("Vrancea", 1L);
    }

    public static CountyRequestDTO aCountyRequestWithName(String name) {
        return new CountyRequestDTO(name, 1L);
    }

    public static CountyRequestDTO aCountyRequestWithNameAndCountryId(String name, Long countryId) {
        return new CountyRequestDTO(name, countryId);
    }

    public static CountyResponseDTO aCountyResponse() {
        return new CountyResponseDTO(1L, "Vrancea", aCountryDTO());
    }
    public static CountyResponseDTO aCountyResponseWithIdAndName(Long id, String name) {
        return new CountyResponseDTO(id, name, aCountryDTO());
    }

    // -------------------  ENTITY  -------------------

    public static County aCounty(){
        return aCountyWithId(1L);
    }

    public static County aCountyWithId(Long id) {
        County county = new County();
        county.setId(id);
        county.setName("Vrancea");
        county.setCountry(aCountry());
        return county;
    }

    public static County aCountyWithoutId() {
        County county = new County();
        county.setName("Vrancea");
        county.setCountry(aCountry());
        return county;
    }
}

package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.web.dto.county_dto.CountyDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.county_dto.CountyResponseDTO;
import ro.insurance.buildinginsurance.domain.entity.County;

import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountry;
import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountryDTO;

public class CountyTestDataFactory {

    private CountyTestDataFactory() {}

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

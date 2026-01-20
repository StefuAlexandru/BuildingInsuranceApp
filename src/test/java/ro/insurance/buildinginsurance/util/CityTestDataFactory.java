package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.County;

import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCounty;
import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCountyDTO;

public class CityTestDataFactory {

    private CityTestDataFactory() {}

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



}

package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.cityDto.CityDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientDTO;
import ro.insurance.buildinginsurance.entity.City;
import ro.insurance.buildinginsurance.entity.County;

import static ro.insurance.buildinginsurance.util.CountyTestDataFactory.aCounty;

public class CityTestDataFactory {

    private CityTestDataFactory() {}

    // -------------------  DTOS  -------------------

    public static CityRequestDTO aCityRequestDTO() {
        return new CityRequestDTO("Adjud", 1L);
    }

    public static CityDTO aCityDTO(){
        return new CityDTO(1L, "Adjud", "Vrancea", "Romania");
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

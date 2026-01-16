package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.entity.Country;

public class CountryTestDataFactory {

    private CountryTestDataFactory() {}

    // -------------------  ENTITY  -------------------

    public static Country aCountry(){
        Country country = new Country();
        country.setId(1L);
        country.setName("Romania");
        return country;
    }
}

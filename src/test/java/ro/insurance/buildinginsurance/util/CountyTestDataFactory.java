package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.entity.Country;
import ro.insurance.buildinginsurance.entity.County;

import static ro.insurance.buildinginsurance.util.CountryTestDataFactory.aCountry;

public class CountyTestDataFactory {

    private CountyTestDataFactory() {}

    // -------------------  ENTITY  -------------------

    public static County aCounty(){
        County county = new County();
        Country country = aCountry();
        county.setId(1L);
        county.setName("Vrancea");
        county.setCountry(country);
        return county;
    }
}

package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.countryDto.*;
import ro.insurance.buildinginsurance.entity.Country;

public class CountryTestDataFactory {

    private CountryTestDataFactory() {}

    // ------------------- DTOS -------------------

    public static CountryRequestDTO aCountryRequest() {
        return new CountryRequestDTO("Romania");
    }

    public static CountryRequestDTO aCountryRequestWithName(String name) {
        return new CountryRequestDTO(name);
    }

    public static CountryDTO aCountryDTO() {
        return new CountryDTO(1L, "Romania");
    }

    // -------------------  ENTITY  -------------------

    public static Country aCountry() {
        return aCountryWithId(1L);
    }

    public static Country aCountryWithId(Long id) {
        Country c = new Country();
        c.setId(id);
        c.setName("Romania");
        return c;
    }

    public static Country aCountryWithoutId() {
        Country c = new Country();
        c.setName("Romania");
        return c;
    }
}

package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.dto.buildingDto.BuildingDTO;
import ro.insurance.buildinginsurance.enums.BuildingType;

import java.math.BigDecimal;

import static ro.insurance.buildinginsurance.util.CityTestDataFactory.aCityDTO;

public class BuildingTestDataFactory {

    private BuildingTestDataFactory() {}

    // -------------------  DTOS  -------------------

    public static BuildingDTO aBuildingDTO(){
        return new BuildingDTO(
                100L,
                aCityDTO(),
                "Street",
                "10A",
                2000,
                BuildingType.OFFICE,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("50000"),
                false,
                true
        );
    }

    // -------------------  ENTITY  -------------------
}

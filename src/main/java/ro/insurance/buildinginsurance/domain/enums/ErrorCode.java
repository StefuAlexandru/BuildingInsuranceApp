package ro.insurance.buildinginsurance.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 409
    CLIENT_ALREADY_EXIST(HttpStatus.CONFLICT, "Client already exists"),
    CITY_ALREADY_EXIST(HttpStatus.CONFLICT, "City already exists"),
    CLIENT_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "Client has dependencies"),
    BUILDING_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "Building has dependencies"),
    CITY_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "City has dependencies"),
    NO_CHANGES(HttpStatus.CONFLICT, "No changes"),
    ID_NUMBER_REQUIRED_WHEN_TYPE_CHANGES(HttpStatus.CONFLICT, "Id number required when type is changed"),
    COUNTY_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "County has dependencies"),
    COUNTY_ALREADY_EXIST(HttpStatus.CONFLICT, "County already exists"),
    COUNTRY_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "Country has dependencies"),
    COUNTRY_ALREADY_EXIST(HttpStatus.CONFLICT, "Country already exists"),

    // 404
    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "City not found"),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Client not found"),
    BUILDING_NOT_FOUND(HttpStatus.NOT_FOUND, "Building not found"),
    COUNTY_NOT_FOUND(HttpStatus.NOT_FOUND, "County not found"),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "Country not found"),

    // 400
    ADDRESS_INCOMPLETE(HttpStatus.BAD_REQUEST, "Address incomplete"),
    CHANGED_BY_REQUIRED(HttpStatus.BAD_REQUEST, "Change by required"),;


    private final HttpStatus status;
    private final String message;
}

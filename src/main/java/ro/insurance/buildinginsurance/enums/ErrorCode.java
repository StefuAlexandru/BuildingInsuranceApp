package ro.insurance.buildinginsurance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    CLIENT_ALREADY_EXIST(HttpStatus.CONFLICT, "Client already exists"),
    CLIENT_HAS_DEPENDENCIES(HttpStatus.CONFLICT, "Client has dependencies"),
    NO_CHANGES(HttpStatus.CONFLICT, "No changes"),
    ID_NUMBER_REQUIRED_WHEN_TYPE_CHANGES(HttpStatus.CONFLICT, "Id number required when type is changed"),

    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "City not found"),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Client not found"),

    ADDRESS_INCOMPLETE(HttpStatus.BAD_REQUEST, "Address incomplete"),
    CHANGED_BY_REQUIRED(HttpStatus.BAD_REQUEST, "Change by required"),;


    private final HttpStatus status;
    private final String message;
}

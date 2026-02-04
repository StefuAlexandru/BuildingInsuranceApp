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
    CURRENCY_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Currency code already exists"),
    CURRENCY_IN_USE_BY_ACTIVE_POLICIES(HttpStatus.CONFLICT, "Currency in use by active policies"),
    FEE_CONFIGURATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Fee configuration already exists"),
    FEE_CONFIGURATION_OVERLAP(HttpStatus.CONFLICT, "Fee configuration overlap"),
    DUPLICATE_RISK_FACTOR_CONFIG(HttpStatus.CONFLICT, "Duplicate risk factor configuration"),
    RISK_FACTOR_CONFIGURATION_OVERLAP(HttpStatus.CONFLICT, "Risk factor configuration overlap"),
    BUILDING_NOT_OWNED_BY_CLIENT(HttpStatus.CONFLICT, "Building not owned by client"),
    POLICY_NOT_ACTIVATABLE(HttpStatus.CONFLICT, "Policy not activatable"),
    POLICY_INVALID_STATE(HttpStatus.CONFLICT, "Policy invalid state"),
    POLICY_START_DATE_IN_PAST(HttpStatus.CONFLICT, "Policy start date in past"),
    BROKER_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Broker code already exists"),
    POLICY_NOT_CANCELLABLE(HttpStatus.CONFLICT, "Policy not cancellable"),
    POLICY_CANCELLATION_DATE_OUT_OF_RANGE(HttpStatus.CONFLICT, "Policy is out of availability range"),
    BROKER_IS_NOT_ACTIVE(HttpStatus.CONFLICT, "Broker is not active"),

    // 404
    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "City not found"),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Client not found"),
    BUILDING_NOT_FOUND(HttpStatus.NOT_FOUND, "Building not found"),
    COUNTY_NOT_FOUND(HttpStatus.NOT_FOUND, "County not found"),
    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "Country not found"),
    CURRENCY_NOT_FOUND(HttpStatus.NOT_FOUND, "Currency not found"),
    FEE_CONFIGURATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Fee configuration not found"),
    RISK_FACTOR_CONFIGURATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Risk factor configuration not found"),
    BROKER_NOT_FOUND(HttpStatus.NOT_FOUND, "Broker not found"),
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "Policy not found"),

    // 400
    ADDRESS_INCOMPLETE(HttpStatus.BAD_REQUEST, "Address incomplete"),
    CHANGED_BY_REQUIRED(HttpStatus.BAD_REQUEST, "Change by required"),
    INVALID_DATES(HttpStatus.BAD_REQUEST, "Invalid dates"),
    INVALID_RISK_FACTOR_SCOPE(HttpStatus.BAD_REQUEST, "Invalid risk factor scope"),
    CURRENCY_IS_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Currency is not active"),
    ;


    private final HttpStatus status;
    private final String message;
}

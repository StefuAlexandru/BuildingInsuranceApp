package ro.insurance.buildinginsurance.web.error;


import lombok.extern.slf4j.Slf4j;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import tools.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.ClientType;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ErrorModel>> handleBusinessException(BusinessException ex){
        HttpStatus status = ex.getErrors().stream()
                .map(error -> ErrorCode.valueOf(error.getCode()).getStatus())
                .findFirst()
                .orElse(HttpStatus.BAD_REQUEST);

        log.warn("BusinessException -> status={}, errors={}", status, ex.getErrors());

        return new ResponseEntity<>(ex.getErrors(),status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleFieldValidationException(MethodArgumentNotValidException ex){
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ErrorModel> errors = new ArrayList<>();
        fieldErrors.forEach(fieldError -> {
            ErrorModel errorModel = new ErrorModel(fieldError.getCode(), fieldError.getDefaultMessage());
            errors.add(errorModel);
        });

        log.warn("Validation failed -> {} errors: {}", errors.size(), errors);

        return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<ErrorModel>> handleJsonParseError(HttpMessageNotReadableException ex) {
        List<ErrorModel> errors = new ArrayList<>();

        ex.getMostSpecificCause();
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            var targetType = invalidFormatException.getTargetType();

            if (targetType.equals(BuildingType.class)) {
                errors.add(new ErrorModel(
                        "InvalidBuildingType",
                        "Building type must be one of: " + Arrays.toString(BuildingType.values())
                ));

                log.warn("JSON parse error -> InvalidBuildingType, value='{}'", invalidFormatException.getValue());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            if (targetType.equals(ClientType.class)) {
                errors.add(new ErrorModel(
                        "InvalidClientType",
                        "Client type must be one of: " + Arrays.toString(ClientType.values())
                ));

                log.warn("JSON parse error -> InvalidClientType, value='{}'", invalidFormatException.getValue());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            if (targetType.equals(FeeType.class)) {
                errors.add(new ErrorModel(
                        "InvalidFeeType",
                        "Fee type must be one of: " + Arrays.toString(FeeType.values())
                ));

                log.warn("JSON parse error -> InvalidFeeType, value='{}'", invalidFormatException.getValue());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
        }

        errors.add(new ErrorModel("InvalidRequestBody", "Malformed JSON"));
        log.warn("JSON parse error -> Malformed JSON: {}", cause.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ErrorModel>> handleUnexpected(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of(new ErrorModel("INTERNAL_ERROR", "Something went wrong")));
    }
}

package ro.insurance.buildinginsurance.error;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ro.insurance.buildinginsurance.enums.BuildingType;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.enums.ErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<List<ErrorModel>> handleBusinessException(BusinessException ex){
        HttpStatus status = ex.getErrors().stream()
                .map(error -> ErrorCode.valueOf(error.getCode()).getStatus())
                .findFirst()
                .orElse(HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<ErrorModel>> handleJsonParseError(HttpMessageNotReadableException ex) {
        List<ErrorModel> errors = new ArrayList<>();

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();

            if (targetType == BuildingType.class) {
                errors.add(new ErrorModel(
                        "InvalidBuildingType",
                        "Building type must be one of: " + Arrays.toString(ro.insurance.buildinginsurance.enums.BuildingType.values())
                ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            if (targetType == ClientType.class) {
                errors.add(new ErrorModel(
                        "InvalidClientType",
                        "Client type must be one of: " + Arrays.toString(ro.insurance.buildinginsurance.enums.ClientType.values())
                ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
        }

        errors.add(new ErrorModel("InvalidRequestBody", "Malformed JSON"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

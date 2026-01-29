package ro.insurance.buildinginsurance.web.error;


import lombok.extern.slf4j.Slf4j;
import ro.insurance.buildinginsurance.domain.enums.*;
import tools.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    private static final Map<Class<?>,String> ENUM_ERROR_CODES = Map.of(
            BuildingType.class, "InvalidBuildingType",
            ClientType.class, "InvalidClientType",
            FeeType.class, "InvalidFeeType",
            RiskFactorLevel.class, "InvalidRiskFactorLevel"
    );

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
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            var targetType = invalidFormatException.getTargetType();

            String code = ENUM_ERROR_CODES.get(targetType);

            if (code != null && targetType.isEnum()) {
                String message = "Value must be one of: " + Arrays.toString(targetType.getEnumConstants());

                log.warn("JSON parse error -> {}, value='{}'", code, invalidFormatException.getValue());
                return ResponseEntity.badRequest().body(List.of(new ErrorModel(code, message)));
            }
        }

        log.warn("JSON parse error -> Malformed JSON: {}", cause.getMessage());
        return ResponseEntity.badRequest().body(List.of(new ErrorModel("InvalidRequestBody", "Malformed JSON")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ErrorModel>> handleUnexpected(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of(new ErrorModel("INTERNAL_ERROR", "Something went wrong")));
    }
}

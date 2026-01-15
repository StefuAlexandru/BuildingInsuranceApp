package ro.insurance.buildinginsurance.error;

import lombok.Getter;
import ro.insurance.buildinginsurance.enums.ErrorCode;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final List<ErrorModel> errors;

    public BusinessException(ErrorCode code) {
        this(List.of(new ErrorModel(code)));
    }

    public BusinessException(List<ErrorModel> errors) {
        super("Validation failed");
        this.errors = List.copyOf(errors);
    }

}

package ro.insurance.buildinginsurance.error;

import lombok.Data;
import lombok.NoArgsConstructor;
import ro.insurance.buildinginsurance.enums.ErrorCode;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ErrorModel implements Serializable {
    private String code;
    private String message;

    public ErrorModel(ErrorCode errorCode) {
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public ErrorModel(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

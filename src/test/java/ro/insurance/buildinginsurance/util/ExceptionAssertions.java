package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ExceptionAssertions {
    private ExceptionAssertions() {}

    public static void assertHasError(BusinessException ex, ErrorCode code) {
        assertTrue(ex.getErrors().stream().anyMatch(e -> code.name().equals(e.getCode())));
    }
}

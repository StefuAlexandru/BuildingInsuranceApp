package ro.insurance.buildinginsurance.web.dto.buildingDto;

import jakarta.validation.constraints.*;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

import java.math.BigDecimal;

public record BuildingPatchDTO(
    Long cityId,

    @Size(max = 255, message = "Street must be at most 255 characters")
    String street,

    @Size(max = 50, message = "Number must be at most 50 characters")
    String number,

    @Min(value = 1800, message = "Construction year must be >= 1800")
    Integer constructionYear,

    BuildingType buildingType,

    @Min(value = 0, message = "Floors must be >= 0")
    Integer floors,

    @DecimalMin(value = "0.01", message = "Surface area must be > 0")
    @Digits(integer = 10, fraction = 2, message = "Surface area must have up to 10 digits and 2 decimals")
    BigDecimal surfaceArea,

    @DecimalMin(value = "0.01", message = "Insured value must be > 0")
    @Digits(integer = 12, fraction = 2, message = "Insured value must have up to 12 digits and 2 decimals")
    BigDecimal insuredValue,

    Boolean floodZone,
    Boolean earthquakeZone
) {}

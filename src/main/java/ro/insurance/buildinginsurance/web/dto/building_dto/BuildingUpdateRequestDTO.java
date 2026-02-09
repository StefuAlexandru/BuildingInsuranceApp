package ro.insurance.buildinginsurance.web.dto.building_dto;

import jakarta.validation.constraints.*;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

import java.math.BigDecimal;

public record BuildingUpdateRequestDTO(
        @NotNull(message = "City id cannot be null")
        Long cityId,

        @NotBlank(message = "Street cannot be blank")
        @Size(max = 255, message = "Street must be at most 255 characters")
        String street,

        @NotBlank(message = "Number cannot be blank")
        @Size(max = 50, message = "Number must be at most 50 characters")
        String number,

        @NotNull(message = "Construction year cannot be null")
        @Min(value = 1800, message = "Construction year must be >= 1800")
        Integer constructionYear,

        @NotNull(message = "Building type cannot be null")
        BuildingType buildingType,

        @Min(value = 0, message = "Floors must be >= 0")
        Integer floors,

        @NotNull(message = "Surface area cannot be null")
        @DecimalMin(value = "0.01", message = "Surface area must be > 0")
        @Digits(integer = 10, fraction = 2, message = "Surface area must have up to 10 digits and 2 decimals")
        BigDecimal surfaceArea,

        @NotNull(message = "Insured value cannot be null")
        @DecimalMin(value = "0.01", message = "Insured value must be > 0")
        @Digits(integer = 12, fraction = 2, message = "Insured value must have up to 12 digits and 2 decimals")
        BigDecimal insuredValue,

        @NotNull(message = "Flood zone is required when updating")
        Boolean floodZone,
        @NotNull(message = "Earthquake zone is required when updating")
        Boolean earthquakeZone
) {}


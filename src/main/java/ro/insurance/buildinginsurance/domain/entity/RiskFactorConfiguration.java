package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;
import ro.insurance.buildinginsurance.domain.enums.RiskFactorLevel;
import ro.insurance.buildinginsurance.util.ValidityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "risk_factor_configurations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_level_ref_building_type_effective_from",
                columnNames = {"level", "reference_id", "building_type", "effective_from"}
        )
)
public class RiskFactorConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 32)
    private RiskFactorLevel level;

    // For City/County/Country: stores the referenced entity id
    private Long referenceId;

    // For BUILDING_TYPE: stores enum name like "HOUSE","APARTAMENT"
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BuildingType buildingType;

    @Column(nullable = false,precision = 7, scale = 4)
    private BigDecimal adjustmentPercentage;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private boolean isActive = true;

    public boolean isActiveOn(LocalDate date) {
        return ValidityUtils.isActiveOn(effectiveFrom, effectiveTo, date);
    }
}

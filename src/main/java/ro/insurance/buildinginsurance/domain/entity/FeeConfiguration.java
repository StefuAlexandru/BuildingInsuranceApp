package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.insurance.buildinginsurance.domain.enums.FeeType;
import ro.insurance.buildinginsurance.util.ValidityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "fee_configurations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_fee_config_fee_type_name_from",
                columnNames = {"fee_type", "name","effective_from"}
        )
)
public class FeeConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 120)
    private String name;

    @Column(nullable = false,length = 32)
    @Enumerated(EnumType.STRING)
    private FeeType feeType;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal percentage;

    @Column( nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private boolean isActive;

    public boolean isActiveOn(LocalDate date) {
        return isActive && ValidityUtils.isActiveOn(effectiveFrom, effectiveTo, date);
    }
}

package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "currencies",
uniqueConstraints = @UniqueConstraint(
        name = "uq_code",
        columnNames = {"code"}
))
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 32)
    private String code;

    @Column(nullable = false,length = 32)
    private String name;

    @Column(nullable = false,precision = 12,scale = 6)
    private BigDecimal exchangeRateToBase;

    private boolean isActive;
}

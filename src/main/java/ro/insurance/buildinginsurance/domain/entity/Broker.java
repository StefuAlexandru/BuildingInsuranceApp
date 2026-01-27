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
@Table( name = "brokers",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_broker_code",
                columnNames = "broker_code"
        )
)
public class Broker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    String brokerCode;

    @Column(nullable = false,length = 120)
    String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    private BigDecimal commissionPct;
}

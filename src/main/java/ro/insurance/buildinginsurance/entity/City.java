package ro.insurance.buildinginsurance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name="uq_county_country_name",
                columnNames = {"country_id","name"}
        )
)
public class County {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 120)
    private String name;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private Country country;
}

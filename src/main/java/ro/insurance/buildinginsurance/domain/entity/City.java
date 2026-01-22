package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name="cities",
        uniqueConstraints = @UniqueConstraint(
                name="uq_city_county_name",
                columnNames = {"county_id","name"}
        )
)
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 120)
    private String name;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    private County county;
}

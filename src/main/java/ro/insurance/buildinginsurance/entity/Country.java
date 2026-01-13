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
        name="countries",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_country_name",
                columnNames = "name"
        )
)
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 120)
    private String name;
}

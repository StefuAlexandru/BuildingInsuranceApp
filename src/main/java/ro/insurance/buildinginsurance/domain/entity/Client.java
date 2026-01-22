package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.insurance.buildinginsurance.domain.enums.ClientType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name="clients",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_client_identification_number",
                columnNames = "identification_number"
        )
)
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,length = 32)
    private String identificationNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    private String addressStreet;
    private String addressNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private City addressCity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private ClientType clientType;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    private List<Building> buildings =  new ArrayList<>();

}

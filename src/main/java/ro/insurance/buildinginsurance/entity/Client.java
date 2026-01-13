package ro.insurance.buildinginsurance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.service.registry.HttpServiceGroup;
import ro.insurance.buildinginsurance.enums.ClientType;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private ClientType ClientType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,length = 32)
    private String identificationNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    private String addressStreet;
    private String addressNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private City addressCity;
}

package ro.insurance.buildinginsurance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "client_change_history")
public class ClientChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Field field;

    @Column(nullable = false)
    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime changedAt;

    public enum Field {
        IDENTIFICATION_NUMBER,
        CLIENT_TYPE
    }
}

package ro.insurance.buildinginsurance.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.insurance.buildinginsurance.domain.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "policies")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PolicyStatus status = PolicyStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Broker broker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal basePremiumAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal finalPremiumAmount;

    private OffsetDateTime activatedAt;
    private OffsetDateTime cancelledAt;

    private String cancellationReason;
}

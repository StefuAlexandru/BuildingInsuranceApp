package ro.insurance.buildinginsurance.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.insurance.buildinginsurance.domain.enums.BuildingType;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private City city;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 50)
    private String number;

    @Column(nullable = false)
    private Integer constructionYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BuildingType buildingType;

    private Integer floors;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal surfaceArea;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal insuredValue;

    private Boolean floodZone;
    private Boolean earthquakeZone;
}
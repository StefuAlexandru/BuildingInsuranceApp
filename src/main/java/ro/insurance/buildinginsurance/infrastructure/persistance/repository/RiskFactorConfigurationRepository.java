    package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;

    import java.time.LocalDate;
    import java.util.List;

    public interface RiskFactorConfigurationRepository extends JpaRepository<RiskFactorConfiguration, Long> {
        @Query(value = """
            SELECT rfc.*
            FROM risk_factor_configurations rfc
            JOIN buildings b ON b.id = :buildingId
            JOIN cities ci ON ci.id = b.city_id
            JOIN counties co ON co.id = ci.county_id
            WHERE rfc.effective_from <= :effectiveDate
              AND (rfc.effective_to IS NULL OR rfc.effective_to >= :effectiveDate)
              AND (
                    (rfc.level = 'BUILDING_TYPE' AND rfc.building_type = b.building_type AND rfc.reference_id IS NULL)
                 OR (rfc.level = 'CITY' AND rfc.reference_id = b.city_id)
                 OR (rfc.level = 'COUNTY' AND rfc.reference_id = ci.county_id)
                 OR (rfc.level = 'COUNTRY' AND rfc.reference_id = co.country_id)
              )
            """, nativeQuery = true)
        List<RiskFactorConfiguration> findRiskFactorsByBuildingIdActiveOn(
                @Param("buildingId") Long buildingId,
                @Param("effectiveDate") LocalDate effectiveDate);

        @Query(value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM risk_factor_configurations rfc
                    WHERE rfc.level = :level
                      AND rfc.effective_from = :effectiveFrom
                      AND (
                            (rfc.reference_id = :referenceId)
                            OR (rfc.reference_id IS NULL AND :referenceId IS NULL)
                          )
                      AND (
                            (rfc.building_type = :buildingType)
                            OR (rfc.building_type IS NULL AND :buildingType IS NULL)
                          )
                )
                """, nativeQuery = true)
        boolean existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFrom(
                @Param("level") String level,
                @Param("referenceId") Long referenceId,
                @Param("buildingType") String buildingType,
                @Param("effectiveFrom") LocalDate effectiveFrom
        );

        @Query(value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM risk_factor_configurations rfc
                    WHERE rfc.level = :level
                      AND rfc.effective_from = :effectiveFrom
                      AND (
                            (rfc.reference_id = :referenceId)
                            OR (rfc.reference_id IS NULL AND :referenceId IS NULL)
                          )
                      AND (
                            (rfc.building_type = :buildingType)
                            OR (rfc.building_type IS NULL AND :buildingType IS NULL)
                          )
                      AND rfc.id <> :id
                )
                """, nativeQuery = true)
        boolean existsByLevelAndReferenceIdAndBuildingTypeAndEffectiveFromAndIdNot(
                @Param("level") String level,
                @Param("referenceId") Long referenceId,
                @Param("buildingType") String buildingType,
                @Param("effectiveFrom") LocalDate effectiveFrom,
                @Param("id") Long id
        );

        @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM risk_factor_configurations rfc
                WHERE rfc.level = :level
                  AND (
                        (rfc.reference_id = :referenceId)
                        OR (rfc.reference_id IS NULL AND :referenceId IS NULL)
                      )
                  AND (
                        (rfc.building_type = :buildingType)
                        OR (rfc.building_type IS NULL AND :buildingType IS NULL)
                      )
                  AND rfc.effective_from < COALESCE(:newEffectiveTo, DATE '9999-12-31')
                  AND COALESCE(rfc.effective_to, DATE '9999-12-31') > :newEffectiveFrom
            )
            """, nativeQuery = true)
        boolean existsOverlappingIntervalForCreate(
                @Param("level") String level,
                @Param("referenceId") Long referenceId,
                @Param("buildingType") String buildingType,
                @Param("newEffectiveFrom") LocalDate newEffectiveFrom,
                @Param("newEffectiveTo") LocalDate newEffectiveTo
        );

        @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM risk_factor_configurations rfc
                WHERE rfc.level = :level
                  AND (
                        (rfc.reference_id = :referenceId)
                        OR (rfc.reference_id IS NULL AND :referenceId IS NULL)
                      )
                  AND (
                        (rfc.building_type = :buildingType)
                        OR (rfc.building_type IS NULL AND :buildingType IS NULL)
                      )
                  AND rfc.id <> :id
                  AND rfc.effective_from < COALESCE(:newEffectiveTo, DATE '9999-12-31')
                  AND COALESCE(rfc.effective_to, DATE '9999-12-31') > :newEffectiveFrom
            )
            """, nativeQuery = true)
        boolean existsOverlappingIntervalForUpdate(
                @Param("id") Long id,
                @Param("level") String level,
                @Param("referenceId") Long referenceId,
                @Param("buildingType") String buildingType,
                @Param("newEffectiveFrom") LocalDate newEffectiveFrom,
                @Param("newEffectiveTo") LocalDate newEffectiveTo
        );



    }

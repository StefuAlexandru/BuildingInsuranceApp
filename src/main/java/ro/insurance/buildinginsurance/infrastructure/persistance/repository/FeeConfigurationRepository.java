package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;

import java.time.LocalDate;
import java.util.List;

public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, Long> {
    @Query(value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM fee_configurations
                    WHERE LOWER(name) = LOWER(:name)
                        AND fee_type = :feeType
                        AND effective_from = :effectiveFrom
                )
                """,nativeQuery=true)
    boolean existsByNameAndFeeTypeAndEffectiveFromIgnoreCase(
            @Param("name") String name,
            @Param("feeType") String feeType,
            @Param("effectiveFrom") LocalDate effectiveFrom
    );

    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM fee_configurations
                WHERE LOWER(name) = LOWER(:name)
                  AND fee_type = :feeType
                  AND effective_from = :effectiveFrom
                  AND id <> :feeId
            )
            """,
            nativeQuery = true)
    boolean existsByNameAndFeeTypeAndEffectiveFromIgnoreCaseAndIdNot(
            @Param("name") String name,
            @Param("feeType") String feeType,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("feeId") Long feeId
    );

    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM fee_configurations
                WHERE fee_type = :feeType
                  AND LOWER(name) = LOWER(:name)
                  AND effective_from < COALESCE(:newEffectiveTo, DATE '9999-12-31')
                  AND COALESCE(effective_to, DATE '9999-12-31') > :newEffectiveFrom
            )
            """,
            nativeQuery = true)
    boolean existsOverlappingIntervalForCreate(
            @Param("feeType") String feeType,
            @Param("name") String name,
            @Param("newEffectiveFrom") LocalDate newEffectiveFrom,
            @Param("newEffectiveTo") LocalDate newEffectiveTo
    );


    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM fee_configurations
                WHERE fee_type = :feeType
                  AND LOWER(name) = LOWER(:name)
                  AND id <> :feeId
                  AND effective_from < COALESCE(:newEffectiveTo, DATE '9999-12-31')
                  AND COALESCE(effective_to, DATE '9999-12-31') > :newEffectiveFrom
            )
            """,
            nativeQuery = true)
    boolean existsOverlappingIntervalForUpdate(
            @Param("feeId") Long feeId,
            @Param("feeType") String feeType,
            @Param("name") String name,
            @Param("newEffectiveFrom") LocalDate newEffectiveFrom,
            @Param("newEffectiveTo") LocalDate newEffectiveTo
    );

    @Query(value = """
        SELECT fc.*
        FROM fee_configurations fc
        WHERE fc.effective_from <= :date
          AND (fc.effective_to IS NULL OR fc.effective_to > :date)
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM fee_configurations fc
        WHERE fc.effective_from <= :date
          AND (fc.effective_to IS NULL OR fc.effective_to > :date)
        """,
            nativeQuery = true)
    Page<FeeConfiguration> getAllFeesActiveOn(@Param("date") LocalDate date, Pageable pageable);

    @Query(value = """
            SELECT fc.*
            FROM fee_configurations fc
            WHERE fc.effective_from <= :date
              AND (fc.effective_to IS NULL OR fc.effective_to >= :date)
        """, nativeQuery = true)
    List<FeeConfiguration> findAllActiveOn(@Param("date") LocalDate date);



}

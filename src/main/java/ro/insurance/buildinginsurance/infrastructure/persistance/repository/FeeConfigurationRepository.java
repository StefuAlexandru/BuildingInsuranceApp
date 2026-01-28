package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.County;
import ro.insurance.buildinginsurance.domain.entity.FeeConfiguration;
import ro.insurance.buildinginsurance.domain.enums.FeeType;

import java.time.LocalDate;

public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, Long> {
    @Query(value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM fee_configurations
                    WHERE LOWER(name) = LOWER(:name)
                        AND fee_type = :feeType
                )
                """,nativeQuery=true)
    boolean existsByNameAndFeeTypeIgnoreCase(@Param("name") String name, @Param("feeType") String feeType);

    @Query(value = """
                SELECT EXISTS(
                    SELECT 1
                    FROM fee_configurations
                    WHERE LOWER(name) = LOWER(:name)
                        AND fee_type = :feeType
                        AND id <> :feeId
                )
                """,nativeQuery=true)
    boolean existsByNameAndFeeTypeIgnoreCaseAndIdNot(@Param("name") String name,@Param("feeType") String feeType,@Param("feeId") Long feeId);

    @Query(value = """
        SELECT fc.*
        FROM fee_configurations fc
        WHERE fc.is_active = true
          AND fc.effective_from <= :date
          AND (fc.effective_to IS NULL OR fc.effective_to >= :date)
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM fee_configurations fc
        WHERE fc.is_active = true
          AND fc.effective_from <= :date
          AND (fc.effective_to IS NULL OR fc.effective_to >= :date)
        """,
            nativeQuery = true)
    Page<FeeConfiguration> getAllFeesActiveOn(@Param("date") LocalDate date, Pageable pageable);

}

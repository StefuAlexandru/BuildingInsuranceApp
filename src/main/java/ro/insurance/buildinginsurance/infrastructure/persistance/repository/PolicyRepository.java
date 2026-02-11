package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.Policy;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy,Long>, JpaSpecificationExecutor<Policy> {
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM policies p
                JOIN currencies c ON c.id = p.currency_id
                WHERE UPPER(c.code) = UPPER(:code)
                  AND p.status = 'ACTIVE'
            )
            """, nativeQuery = true)
    boolean existsActivePolicyByCurrencyCode(@Param("code") String code);

    @Override
    @EntityGraph(attributePaths = {"client", "building", "building.city", "currency"})
    @NonNull
    Page<Policy> findAll(@NonNull Specification<Policy> specification, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {
            "client","building","building.city","building.city.county",
            "building.city.county.country","currency"
    })
    Optional<Policy> findDetailsByIdAndBrokerId(Long id, Long brokerId);

    @Query(value = """
        SELECT p.*
        FROM policies p
        WHERE p.id = :id
          AND p.broker_id = :brokerId
        """, nativeQuery = true)
    Optional<Policy> findByIdAndBrokerId(@Param("id") Long id, @Param("brokerId") Long brokerId);

    @EntityGraph(attributePaths = {
            "client","building","building.city","building.city.county",
            "building.city.county.country","currency"
    })
    Optional<Policy> findDetailsById(Long id);

}

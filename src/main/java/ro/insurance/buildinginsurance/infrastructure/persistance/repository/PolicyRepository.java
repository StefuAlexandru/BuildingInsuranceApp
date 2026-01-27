package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy,Long> {
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
}

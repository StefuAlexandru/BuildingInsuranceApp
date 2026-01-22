package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.Building;

import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query(
            value = """
                SELECT b.*
                FROM buildings b
                WHERE b.client_id = :clientId
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM buildings b
                WHERE b.client_id = :clientId
            """, nativeQuery = true)
    Page<Building> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query(
            value = """
                SELECT b.*
                FROM buildings b
                JOIN cities ci ON ci.id = b.city_id
                JOIN counties co ON co.id = ci.county_id
                JOIN countries ctr ON ctr.id = co.country_id
                WHERE b.id = :buildingId
            """, nativeQuery = true)
    Optional<Building> findDetailsById(@Param("buildingId") Long buildingId);
}

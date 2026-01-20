package ro.insurance.buildinginsurance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.County;

@Repository
public interface CountyRepository extends JpaRepository<County, Long> {
    @Query(value = """
                SELECT EXISTS(
                        SELECT 1
                        FROM counties
                        WHERE LOWER(name) = LOWER(:name)
                            AND country_id = :countryId)
            """, nativeQuery = true)
    boolean existsByNameIgnoreCaseAndCountryId(@Param("name") String name, @Param("countryId") Long countryId);

    @Query(value = """
                SELECT *
                FROM counties
                WHERE LOWER(name) LIKE CONCAT('%',LOWER(:countyName),'%')
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM counties
                WHERE LOWER(name) LIKE CONCAT('%',LOWER(:countyName),'%')
            """,
            nativeQuery = true)
    Page<County> getCountyByPartialName(@Param("countyName") String countyName, Pageable pageable);
}

package ro.insurance.buildinginsurance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    @Query(value = """
                SELECT EXISTS(
                        SELECT 1
                        FROM cities
                        WHERE LOWER(name) = LOWER(:name)
                            AND county_id = :countyId)
            """, nativeQuery = true)
    boolean existsByNameIgnoreCaseAndCountyId(@Param("name") String name, @Param("countyId") Long countyId);

    @Query(value = """
                SELECT *
                FROM cities
                WHERE LOWER(name) LIKE CONCAT('%',LOWER(:cityName),'%')
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM cities
                WHERE LOWER(name) LIKE CONCAT('%',LOWER(:cityName),'%')
            """,
            nativeQuery = true)
    Page<City> getCityByPartialName(@Param("cityName") String cityName, Pageable pageable);
}
package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.domain.entity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM countries
                WHERE LOWER(name) = LOWER(:name)
            )
            """, nativeQuery = true)
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query(
            value = """
                SELECT *
                FROM countries
                WHERE LOWER(name) LIKE CONCAT('%', LOWER(:countryName), '%')
            """,
            countQuery = """
                SELECT COUNT(*)
                FROM countries
                WHERE LOWER(name) LIKE CONCAT('%', LOWER(:countryName), '%')
            """, nativeQuery = true)
    Page<Country> getCountryByPartialName(@Param("countryName") String countryName, Pageable pageable);

}

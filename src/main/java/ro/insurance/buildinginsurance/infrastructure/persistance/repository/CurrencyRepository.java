package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    @Query(value = """
                SELECT EXISTS(
                      SELECT 1
                      FROM currencies
                      WHERE LOWER(code) = LOWER(:code)
                )
                """,nativeQuery = true)
    boolean existsByCodeIgnoreCase(@Param("code") String code);

}

package ro.insurance.buildinginsurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}

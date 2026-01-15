package ro.insurance.buildinginsurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.County;

@Repository
public interface CountyRepository extends JpaRepository<County, Long> {
}

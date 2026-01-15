package ro.insurance.buildinginsurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.Building;
@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
}

package ro.insurance.buildinginsurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.insurance.buildinginsurance.entity.ClientChangeHistory;

public interface ClientChangeHistoryRepository extends JpaRepository<ClientChangeHistory, Long> {

}

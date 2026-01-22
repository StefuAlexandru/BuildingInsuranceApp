package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.insurance.buildinginsurance.domain.entity.ClientChangeHistory;

public interface ClientChangeHistoryRepository extends JpaRepository<ClientChangeHistory, Long> {

}

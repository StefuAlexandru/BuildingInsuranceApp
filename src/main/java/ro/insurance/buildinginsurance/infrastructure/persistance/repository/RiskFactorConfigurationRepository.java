package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.insurance.buildinginsurance.domain.entity.RiskFactorConfiguration;

public interface RiskFactorConfigurationRepository extends JpaRepository<RiskFactorConfiguration, Long> {

}

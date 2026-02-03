package ro.insurance.buildinginsurance.infrastructure.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.insurance.buildinginsurance.domain.entity.Broker;

public interface BrokerRepository extends JpaRepository<Broker, Long> {
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM brokers
                WHERE LOWER(broker_code) = LOWER(:brokerCode)
            )
            """, nativeQuery = true)
    boolean existsByBrokerCodeIgnoreCase(@Param("brokerCode") String brokerCode);
}
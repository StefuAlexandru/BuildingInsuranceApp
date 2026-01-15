package ro.insurance.buildinginsurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.insurance.buildinginsurance.entity.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM clients
                WHERE identification_number = :identificationNumber
            )""",nativeQuery = true)
    boolean existsByIdentificationNumber(@Param("identificationNumber") String identificationNumber);

    @Query(value = """
            SELECT *
            FROM clients
            WHERE lower(name) = lower(:name) AND identification_number = :identificationNumber
            """,nativeQuery = true)
    Optional<Client> findByNameAndIdentificationNumber(@Param("name") String name, @Param("identificationNumber") String identificationNumber);

    @Query(value = """
            SELECT EXISTS(
                        SELECT 1
                        FROM clients
                        WHERE identification_number = :identificationNumber AND id <> :clientId
            )
            """,nativeQuery = true)
    boolean existsByIdentificationNumberAndIdNot(@Param("identificationNumber") String identificationNumber, @Param("clientId") Long clientId);

}

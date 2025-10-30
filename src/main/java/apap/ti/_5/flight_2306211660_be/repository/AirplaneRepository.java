package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import apap.ti._5.flight_2306211660_be.model.Airplane;

public interface AirplaneRepository extends JpaRepository<Airplane, String> {

    List<Airplane> findByModelContainingIgnoreCase(String model);

    List<Airplane> findByAirlineId(String airlineId);

    List<Airplane> findByIsDeleted(Boolean isDeleted);

    @Query("SELECT a FROM Airplane a WHERE a.airlineId = :airlineId AND a.isDeleted = false")
    List<Airplane> findActiveByAirlineId(@Param("airlineId") String airlineId);

    @Query("SELECT a FROM Airplane a WHERE a.id = :airplaneId AND a.isDeleted = false")
    Airplane findActiveById(@Param("airplaneId") String airplaneId);

    boolean existsByIdAndIsDeleted(String id, Boolean isDeleted);
}

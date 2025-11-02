package apap.ti._5.flight_2306211660_be.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {

    @Query("SELECT f FROM Flight f WHERE f.airplaneId = :airplaneId AND f.isDeleted = false AND " +
           "((f.departureTime <= :arrivalTime AND f.arrivalTime >= :departureTime) OR " +
           "(f.departureTime >= :departureTime AND f.departureTime <= :arrivalTime))")
    List<Flight> findOverlappingFlights(@Param("airplaneId") String airplaneId,
                                       @Param("departureTime") LocalDateTime departureTime,
                                       @Param("arrivalTime") LocalDateTime arrivalTime);

    @Query("SELECT f FROM Flight f WHERE f.airplaneId = :airplaneId AND f.isDeleted = false AND f.id != :flightId AND " +
           "((f.departureTime <= :arrivalTime AND f.arrivalTime >= :departureTime) OR " +
           "(f.departureTime >= :departureTime AND f.departureTime <= :arrivalTime))")
    List<Flight> findOverlappingFlightsExcludingId(@Param("airplaneId") String airplaneId,
                                                  @Param("flightId") String flightId,
                                                  @Param("departureTime") LocalDateTime departureTime,
                                                  @Param("arrivalTime") LocalDateTime arrivalTime);

    @Query("SELECT MAX(CAST(SUBSTRING(f.id, LENGTH(:airplaneId) + 2) AS int)) FROM Flight f WHERE f.id LIKE CONCAT(:airplaneId, '-%')")
    Integer findMaxFlightNumberByAirplaneId(@Param("airplaneId") String airplaneId);

    List<Flight> findByAirlineIdAndIsDeleted(String airlineId, Boolean isDeleted);

    List<Flight> findByIsDeleted(Boolean isDeleted);

    List<Flight> findByAirplaneIdAndIsDeleted(String airplaneId, Boolean isDeleted);
}

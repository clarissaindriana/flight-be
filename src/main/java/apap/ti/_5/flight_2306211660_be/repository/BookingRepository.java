package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByFlightIdAndIsDeleted(String flightId, Boolean isDeleted);

    List<Booking> findByClassFlightIdAndIsDeleted(Integer classFlightId, Boolean isDeleted);

    List<Booking> findByIsDeleted(Boolean isDeleted);

    @Query("SELECT MAX(CAST(SUBSTRING(b.id, LENGTH(:prefix) + 1) AS int)) FROM Booking b WHERE b.id LIKE CONCAT(:prefix, '%')")
    Integer findMaxBookingNumberByPrefix(@Param("prefix") String prefix);

    @Query("SELECT b FROM Booking b WHERE b.flightId = :flightId AND b.isDeleted = false AND (b.status = 1 OR b.status = 2)")
    List<Booking> findActiveBookingsByFlightId(@Param("flightId") String flightId);
}

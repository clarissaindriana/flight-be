package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.BookingPassengerId;

@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, BookingPassengerId> {

    List<BookingPassenger> findByBookingId(String bookingId);

    List<BookingPassenger> findByPassengerId(UUID passengerId);
}

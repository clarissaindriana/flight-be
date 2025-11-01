package apap.ti._5.flight_2306211660_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

}

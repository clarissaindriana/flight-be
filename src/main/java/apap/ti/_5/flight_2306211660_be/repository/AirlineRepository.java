package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import apap.ti._5.flight_2306211660_be.model.Airline;

public interface AirlineRepository extends JpaRepository<Airline, String> {

    List<Airline> findByNameContainingIgnoreCase(String name);

}

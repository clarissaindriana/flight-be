package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Airport;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {

    List<Airport> findByNameContainingIgnoreCase(String name);

    List<Airport> findByCityContainingIgnoreCase(String city);

    List<Airport> findByCountryContainingIgnoreCase(String country);
}

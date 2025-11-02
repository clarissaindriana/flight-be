package apap.ti._5.flight_2306211660_be.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    boolean existsByIdPassport(String idPassport);

    Passenger findByIdPassport(String idPassport);
}

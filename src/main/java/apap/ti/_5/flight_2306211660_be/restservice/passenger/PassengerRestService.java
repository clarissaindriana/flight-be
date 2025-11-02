package apap.ti._5.flight_2306211660_be.restservice.passenger;

import java.util.List;
import java.util.UUID;

import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;

public interface PassengerRestService {

    PassengerResponseDTO createPassenger(AddPassengerRequestDTO dto);

    List<PassengerResponseDTO> getAllPassengers();

    PassengerResponseDTO getPassenger(UUID id);

    PassengerResponseDTO updatePassenger(UpdatePassengerRequestDTO dto);

    PassengerResponseDTO deletePassenger(UUID id);
}

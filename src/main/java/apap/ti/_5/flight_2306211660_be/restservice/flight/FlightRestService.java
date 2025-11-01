package apap.ti._5.flight_2306211660_be.restservice.flight;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.flight.AddFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.UpdateFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightResponseDTO;

public interface FlightRestService {

    FlightResponseDTO createFlight(AddFlightRequestDTO dto);

    List<FlightResponseDTO> getAllFlights();

    List<FlightResponseDTO> searchFlightsByAirline(String airlineId);

    FlightResponseDTO getFlight(String id);

    FlightResponseDTO updateFlight(UpdateFlightRequestDTO dto);

    FlightResponseDTO deleteFlight(String id);
}
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

    List<FlightResponseDTO> getAllFlightsWithFilters(String originAirportCode, String destinationAirportCode,
                                                   String airlineId, Integer status, Boolean includeDeleted, String search);

    // Reminder: upcoming flights within interval (hours). If customerId provided, only include flights booked (Paid) by that customer.
    java.util.List<apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightReminderResponseDTO> getFlightReminders(Integer intervalHours, String customerUserId);

    long getActiveFlightsTodayCount();

    FlightResponseDTO getFlightDetail(String id);

    FlightResponseDTO updateFlight(UpdateFlightRequestDTO dto);

    FlightResponseDTO deleteFlight(String id);
}
package apap.ti._5.flight_2306211660_be.restservice.seat;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.UpdateSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;

public interface SeatRestService {

    SeatResponseDTO createSeat(AddSeatRequestDTO dto);

    List<SeatResponseDTO> getAllSeats();

    List<SeatResponseDTO> getSeatsByClassFlight(Integer classFlightId);

    List<SeatResponseDTO> getSeatsByFlight(String flightId);

    SeatResponseDTO getSeat(Integer id);

    SeatResponseDTO updateSeat(UpdateSeatRequestDTO dto);

    SeatResponseDTO deleteSeat(Integer id);
}

package apap.ti._5.flight_2306211660_be.restservice.seat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.UpdateSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;

@Service
public class SeatRestServiceImpl implements SeatRestService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ClassFlightRepository classFlightRepository;

    @Override
    public SeatResponseDTO createSeat(AddSeatRequestDTO dto) {
        Seat seat = Seat.builder()
                .classFlightId(dto.getClassFlightId())
                .seatCode(dto.getSeatCode())
                .isBooked(false)
                .build();

        return convertToSeatResponseDTO(seatRepository.save(seat));
    }

    @Override
    public List<SeatResponseDTO> getAllSeats() {
        List<Seat> seats = seatRepository.findAll();
        return seats.stream()
                .map(this::convertToSeatResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponseDTO> getSeatsByClassFlight(Integer classFlightId) {
        // This would need a custom query in the repository
        // For now, we'll filter in memory
        List<Seat> seats = seatRepository.findAll();
        return seats.stream()
                .filter(seat -> classFlightId.equals(seat.getClassFlightId()))
                .map(this::convertToSeatResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SeatResponseDTO getSeat(Integer id) {
        Seat seat = seatRepository.findById(id).orElse(null);
        if (seat == null) {
            return null;
        }
        return convertToSeatResponseDTO(seat);
    }

    @Override
    public SeatResponseDTO updateSeat(UpdateSeatRequestDTO dto) {
        Seat seat = seatRepository.findById(dto.getId()).orElse(null);

        if (seat == null) return null;

        seat = seat.toBuilder()
                .passengerId(dto.getPassengerId())
                .isBooked(dto.getPassengerId() != null)
                .build();

        return convertToSeatResponseDTO(seatRepository.save(seat));
    }

    @Override
    public SeatResponseDTO deleteSeat(Integer id) {
        Seat seat = seatRepository.findById(id).orElse(null);
        if (seat == null) {
            return null;
        }

        seatRepository.delete(seat);
        return convertToSeatResponseDTO(seat);
    }

    @Override
    public List<SeatResponseDTO> getSeatsByFlight(String flightId) {
        // Get all class flights for this flight
        List<ClassFlight> classFlights = classFlightRepository.findAll().stream()
                .filter(cf -> flightId.equals(cf.getFlightId()))
                .toList();

        // Get all seats for these class flights
        List<Integer> classFlightIds = classFlights.stream()
                .map(ClassFlight::getId)
                .toList();

        List<Seat> seats = seatRepository.findAll().stream()
                .filter(seat -> classFlightIds.contains(seat.getClassFlightId()))
                .toList();

        return seats.stream()
                .map(this::convertToSeatResponseDTO)
                .collect(Collectors.toList());
    }

    private SeatResponseDTO convertToSeatResponseDTO(Seat seat) {
        return SeatResponseDTO.builder()
                .id(seat.getId())
                .classFlightId(seat.getClassFlightId())
                .passengerId(seat.getPassengerId())
                .seatCode(seat.getSeatCode())
                .isBooked(seat.getIsBooked())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }
}

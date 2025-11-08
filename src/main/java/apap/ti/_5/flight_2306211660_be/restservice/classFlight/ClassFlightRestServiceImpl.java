package apap.ti._5.flight_2306211660_be.restservice.classFlight;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;

@Service
public class ClassFlightRestServiceImpl implements ClassFlightRestService {

    @Autowired
    private ClassFlightRepository classFlightRepository;

    @Autowired
    private SeatRestService seatRestService;

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public ClassFlightResponseDTO createClassFlight(AddClassFlightRequestDTO dto) {
        ClassFlight classFlight = ClassFlight.builder()
                .flightId(dto.getFlightId())
                .classType(dto.getClassType())
                .seatCapacity(dto.getSeatCapacity())
                .availableSeats(dto.getSeatCapacity())
                .price(dto.getPrice())
                .build();

        return convertToClassFlightResponseDTO(classFlightRepository.save(classFlight));
    }

    @Override
    public List<ClassFlightResponseDTO> getAllClassFlights() {
        List<ClassFlight> classFlights = classFlightRepository.findAll();
        return classFlights.stream()
                .map(this::convertToClassFlightResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassFlightResponseDTO> getClassFlightsByFlight(String flightId) {
        List<ClassFlight> classFlights = classFlightRepository.findAll();
        return classFlights.stream()
                .filter(cf -> flightId.equals(cf.getFlightId()))
                .map(this::convertToClassFlightResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClassFlightResponseDTO getClassFlight(Integer id) {
        ClassFlight classFlight = classFlightRepository.findById(id).orElse(null);
        if (classFlight == null) {
            return null;
        }
        return convertToClassFlightResponseDTO(classFlight);
    }

    @Override
    public ClassFlightResponseDTO getClassFlightDetail(Integer id) {
        ClassFlight classFlight = classFlightRepository.findById(id).orElse(null);
        if (classFlight == null) {
            return null;
        }
        return convertToClassFlightDetailResponseDTO(classFlight);
    }

    @Override
    public ClassFlightResponseDTO updateClassFlight(UpdateClassFlightRequestDTO dto) {
        ClassFlight classFlight = classFlightRepository.findById(dto.getId()).orElse(null);

        if (classFlight == null) return null;

        int oldCap = classFlight.getSeatCapacity();
        int newCap = dto.getSeatCapacity();

        // Fetch existing seats for this class flight
        final Integer classFlightId = classFlight.getId();
        List<Seat> existingSeats = seatRepository.findAll().stream()
                .filter(seat -> seat.getClassFlightId().equals(classFlightId))
                .sorted(Comparator.comparing(Seat::getSeatCode))
                .toList();

        // Validation: new capacity cannot be less than currently booked seats
        long bookedCount = existingSeats.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsBooked()))
                .count();
        if (newCap < bookedCount) {
            throw new IllegalStateException("Cannot set seat capacity below currently booked seats (" + bookedCount + ")");
        }

        if (newCap > oldCap) {
            // Add new seats with sequential codes
            String classType = classFlight.getClassType() == null ? "" : classFlight.getClassType().toLowerCase();
            String prefix;
            switch (classType) {
                case "economy": prefix = "EC"; break;
                case "business": prefix = "BU"; break;
                case "first": prefix = "FI"; break;
                default: prefix = "EC"; break;
            }
            for (int i = oldCap + 1; i <= newCap; i++) {
                String code = String.format("%s%03d", prefix, i);
                AddSeatRequestDTO seatDto = AddSeatRequestDTO.builder()
                        .classFlightId(classFlight.getId())
                        .seatCode(code)
                        .build();
                seatRestService.createSeat(seatDto);
            }
        } else if (newCap < oldCap) {
            // Determine seats to remove: choose highest-numbered UNBOOKED seats only
            int seatsToRemove = oldCap - newCap;

            List<Seat> removable = existingSeats.stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getIsBooked()))
                    .sorted(Comparator.comparing(Seat::getSeatCode).reversed())
                    .limit(seatsToRemove)
                    .toList();

            if (removable.size() < seatsToRemove) {
                throw new IllegalStateException("Cannot decrease seat capacity: not enough unbooked tail seats available to remove");
            }

            // Delete the selected seats
            for (Seat s : removable) {
                seatRepository.delete(s);
            }
        }

        // Recalculate available seats after adjustments
        List<Seat> seatsAfter = seatRepository.findAll().stream()
                .filter(seat -> seat.getClassFlightId().equals(classFlightId))
                .toList();
        int availableSeats = (int) seatsAfter.stream().filter(seat -> !Boolean.TRUE.equals(seat.getIsBooked())).count();

        classFlight = classFlight.toBuilder()
                .seatCapacity(newCap)
                .availableSeats(availableSeats)
                .price(dto.getPrice())
                .build();

        return convertToClassFlightResponseDTO(classFlightRepository.save(classFlight));
    }

    @Override
    public ClassFlightResponseDTO deleteClassFlight(Integer id) {
        ClassFlight classFlight = classFlightRepository.findById(id).orElse(null);
        if (classFlight == null) {
            return null;
        }

        classFlightRepository.delete(classFlight);
        return convertToClassFlightResponseDTO(classFlight);
    }

    private ClassFlightResponseDTO convertToClassFlightResponseDTO(ClassFlight classFlight) {
        return ClassFlightResponseDTO.builder()
                .id(classFlight.getId())
                .flightId(classFlight.getFlightId())
                .classType(classFlight.getClassType())
                .seatCapacity(classFlight.getSeatCapacity())
                .availableSeats(classFlight.getAvailableSeats())
                .price(classFlight.getPrice())
                .build();
    }

    private ClassFlightResponseDTO convertToClassFlightDetailResponseDTO(ClassFlight classFlight) {
        // Get seats for this class flight
        List<SeatResponseDTO> seats = seatRestService.getSeatsByClassFlight(classFlight.getId());

        return ClassFlightResponseDTO.builder()
                .id(classFlight.getId())
                .flightId(classFlight.getFlightId())
                .classType(classFlight.getClassType())
                .seatCapacity(classFlight.getSeatCapacity())
                .availableSeats(classFlight.getAvailableSeats())
                .price(classFlight.getPrice())
                .seats(seats)
                .build();
    }
}

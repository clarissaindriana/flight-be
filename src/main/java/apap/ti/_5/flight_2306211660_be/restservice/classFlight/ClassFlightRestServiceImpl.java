package apap.ti._5.flight_2306211660_be.restservice.classFlight;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;

@Service
public class ClassFlightRestServiceImpl implements ClassFlightRestService {

    @Autowired
    private ClassFlightRepository classFlightRepository;

    @Autowired
    private SeatRestService seatRestService;

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
        // This would need a custom query in the repository
        // For now, we'll filter in memory
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

        classFlight = classFlight.toBuilder()
                .seatCapacity(dto.getSeatCapacity())
                .availableSeats(dto.getSeatCapacity()) // Reset available seats
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

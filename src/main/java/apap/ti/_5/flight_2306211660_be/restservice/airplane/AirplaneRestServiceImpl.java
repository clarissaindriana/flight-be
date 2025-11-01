package apap.ti._5.flight_2306211660_be.restservice.airplane;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.Airplane;
import apap.ti._5.flight_2306211660_be.repository.AirplaneRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.AddAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.UpdateAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airplane.AirplaneResponseDTO;

@Service
public class AirplaneRestServiceImpl implements AirplaneRestService {

    @Autowired
    private AirplaneRepository airplaneRepository;

    private static final Random random = new Random();

    @Override
    public AirplaneResponseDTO createAirplane(AddAirplaneRequestDTO dto) {
        // Validate manufacture year
        if (dto.getManufactureYear() > LocalDateTime.now().getYear()) {
            throw new IllegalArgumentException("Manufacture year cannot be in the future");
        }

        // Generate unique registration number
        String registrationNumber;
        do {
            registrationNumber = generateRegistrationNumber(dto.getAirlineId());
        } while (airplaneRepository.existsById(registrationNumber)); // check if there is any duplicate in random ABC

        Airplane airplane = Airplane.builder()
                .id(registrationNumber)
                .airlineId(dto.getAirlineId())
                .model(dto.getModel())
                .seatCapacity(dto.getSeatCapacity())
                .manufactureYear(dto.getManufactureYear())
                .build();

        Airplane savedAirplane = airplaneRepository.save(airplane);
        // Force fetch the airline relationship
        airplaneRepository.flush();
        savedAirplane = airplaneRepository.findById(savedAirplane.getId()).orElse(savedAirplane);

        return convertToAirplaneResponseDTO(savedAirplane);
    }

    @Override
    public List<AirplaneResponseDTO> getAllAirplanes() {
        List<Airplane> allAirplanes = airplaneRepository.findAll();
        return allAirplanes.stream()
                .map(airplane -> convertToAirplaneResponseDTO(airplane))
                .collect(Collectors.toList());
    }

    @Override
    public List<AirplaneResponseDTO> searchAirplanesByModel(String model) {
        List<Airplane> airplanes;

        if (model == null || model.trim().isEmpty()) {
            airplanes = airplaneRepository.findAll();
        } else {
            airplanes = airplaneRepository.findByModelContainingIgnoreCase(model.trim());
        }

        return airplanes.stream()
                .map(airplane -> convertToAirplaneResponseDTO(airplane))
                .collect(Collectors.toList());
    }

    @Override
    public AirplaneResponseDTO getAirplane(String id) {
        Airplane airplane = airplaneRepository.findById(id).orElse(null);
        if (airplane == null) {
            return null;
        }
        return convertToAirplaneResponseDTO(airplane);
    }

    @Override
    public AirplaneResponseDTO updateAirplane(UpdateAirplaneRequestDTO dto) {
        Airplane airplane = airplaneRepository.findById(dto.getId()).orElse(null);

        if (airplane == null) return null;

        // Check if airplane is deleted
        if (airplane.getIsDeleted()) {
            throw new IllegalStateException("Cannot update deleted airplane");
        }

        // Validate manufacture year
        if (dto.getManufactureYear() > LocalDateTime.now().getYear()) {
            throw new IllegalArgumentException("Manufacture year cannot be in the future");
        }

        airplane = airplane.toBuilder()
                .airlineId(dto.getAirlineId())
                .model(dto.getModel())
                .seatCapacity(dto.getSeatCapacity())
                .manufactureYear(dto.getManufactureYear())
                .build();

        return convertToAirplaneResponseDTO(airplaneRepository.save(airplane));
    }

    @Override
    public AirplaneResponseDTO deleteAirplane(String id) {
        Airplane airplane = airplaneRepository.findById(id).orElse(null);
        if (airplane == null) {
            return null;
        }

        // Check if airplane is already deleted
        if (airplane.getIsDeleted()) {
            throw new IllegalStateException("Airplane is already deleted");
        }

        // TODO: ONCE FLIGHT IS IMPLEMENTED

        // // Check if airplane is used in active flights
        // List<Flight> activeFlights = flightRepository.findByAirplaneIdAndStatusIn(id, List.of(0, 1, 2)); // Scheduled, In Flight, Delayed
        // if (!activeFlights.isEmpty()) {
        //     throw new IllegalStateException("Cannot delete airplane that is used in active flights");
        // }

        airplane.setIsDeleted(true);
        return convertToAirplaneResponseDTO(airplaneRepository.save(airplane));
    }

    private String generateRegistrationNumber(String airlineId) {
        String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(randomChars.charAt(random.nextInt(randomChars.length())));
        }
        return airlineId + "-" + sb.toString();
    }

    private AirplaneResponseDTO convertToAirplaneResponseDTO(Airplane airplane) {
        return AirplaneResponseDTO.builder()
                .id(airplane.getId())
                .airlineId(airplane.getAirlineId())
                .model(airplane.getModel())
                .seatCapacity(airplane.getSeatCapacity())
                .manufactureYear(airplane.getManufactureYear())
                .createdAt(airplane.getCreatedAt())
                .updatedAt(airplane.getUpdatedAt())
                .isDeleted(airplane.getIsDeleted())
                .build();
    }
}

package apap.ti._5.flight_2306211660_be.restservice.flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.flight_2306211660_be.model.Airplane;
import apap.ti._5.flight_2306211660_be.model.Flight;
import apap.ti._5.flight_2306211660_be.repository.AirplaneRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.AddFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.UpdateFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.classFlight.ClassFlightRestService;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;

@Service
public class FlightRestServiceImpl implements FlightRestService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private ClassFlightRestService classFlightRestService;

    @Autowired
    private SeatRestService seatRestService;

    @Override
    @Transactional
    public FlightResponseDTO createFlight(AddFlightRequestDTO dto) {
        // Validate departure < arrival
        if (dto.getDepartureTime().isAfter(dto.getArrivalTime()) || dto.getDepartureTime().equals(dto.getArrivalTime())) {
            throw new IllegalArgumentException("Departure time must be before arrival time");
        }

        // Validate origin != destination
        if (dto.getOriginAirportCode().equals(dto.getDestinationAirportCode())) {
            throw new IllegalArgumentException("Origin and destination airports cannot be the same");
        }

        // Check if airplane exists and is active
        Airplane airplane = airplaneRepository.findActiveById(dto.getAirplaneId());
        if (airplane == null) {
            throw new IllegalArgumentException("Airplane not found or not active");
        }

        // Check total seat capacity
        int totalRequestedSeats = dto.getClasses().stream()
                .mapToInt(AddClassFlightRequestDTO::getSeatCapacity)
                .sum();
        if (totalRequestedSeats > airplane.getSeatCapacity()) {
            throw new IllegalArgumentException("Total requested seats exceed airplane capacity");
        }

        // Check for overlapping flights
        List<Flight> overlappingFlights = flightRepository.findOverlappingFlights(
                dto.getAirplaneId(), dto.getDepartureTime(), dto.getArrivalTime());
        if (!overlappingFlights.isEmpty()) {
            throw new IllegalArgumentException("Airplane is already scheduled for overlapping flights");
        }

        // Generate flight ID
        String flightId = generateFlightId(dto.getAirplaneId());

        // Create flight
        Flight flight = Flight.builder()
                .id(flightId)
                .airlineId(dto.getAirlineId())
                .airplaneId(dto.getAirplaneId())
                .originAirportCode(dto.getOriginAirportCode())
                .destinationAirportCode(dto.getDestinationAirportCode())
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .terminal(dto.getTerminal())
                .gate(dto.getGate())
                .baggageAllowance(dto.getBaggageAllowance())
                .facilities(dto.getFacilities())
                .status(1) // Scheduled
                .isDeleted(false)
                .build();

        flight = flightRepository.save(flight);

        // Create class flights and seats
        for (AddClassFlightRequestDTO classDto : dto.getClasses()) {
            // Create a new DTO with flightId set
            AddClassFlightRequestDTO classFlightDto = AddClassFlightRequestDTO.builder()
                    .flightId(flightId)
                    .classType(classDto.getClassType())
                    .seatCapacity(classDto.getSeatCapacity())
                    .price(classDto.getPrice())
                    .build();

            var classFlightResponse = classFlightRestService.createClassFlight(classFlightDto);

            // Generate seats for the class flight
            generateSeatsForClass(classFlightResponse.getId(), classDto.getSeatCapacity(), classDto.getClassType());
        }

        return convertToFlightResponseDTO(flight);
    }

    @Override
    public List<FlightResponseDTO> getAllFlights() {
        List<Flight> flights = flightRepository.findByIsDeleted(false);
        return flights.stream()
                .map(this::convertToFlightResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightResponseDTO> searchFlightsByAirline(String airlineId) {
        List<Flight> flights = flightRepository.findByAirlineIdAndIsDeleted(airlineId, false);
        return flights.stream()
                .map(this::convertToFlightResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FlightResponseDTO getFlight(String id) {
        Flight flight = flightRepository.findById(id).orElse(null);
        if (flight == null || flight.getIsDeleted()) {
            return null;
        }
        return convertToFlightResponseDTO(flight);
    }

    @Override
    public List<FlightResponseDTO> getAllFlightsWithFilters(String originAirportCode, String destinationAirportCode,
                                                          String airlineId, Integer status, Boolean includeDeleted) {
        List<Flight> flights;

        if (includeDeleted != null && includeDeleted) {
            flights = flightRepository.findAll();
        } else {
            flights = flightRepository.findByIsDeleted(false);
        }

        // Apply filters
        if (originAirportCode != null && !originAirportCode.trim().isEmpty()) {
            flights = flights.stream()
                    .filter(f -> originAirportCode.equalsIgnoreCase(f.getOriginAirportCode()))
                    .toList();
        }

        if (destinationAirportCode != null && !destinationAirportCode.trim().isEmpty()) {
            flights = flights.stream()
                    .filter(f -> destinationAirportCode.equalsIgnoreCase(f.getDestinationAirportCode()))
                    .toList();
        }

        if (airlineId != null && !airlineId.trim().isEmpty()) {
            flights = flights.stream()
                    .filter(f -> airlineId.equalsIgnoreCase(f.getAirlineId()))
                    .toList();
        }

        if (status != null) {
            flights = flights.stream()
                    .filter(f -> status.equals(f.getStatus()))
                    .toList();
        }

        // Sort by departure time ascending
        flights = flights.stream()
                .sorted((f1, f2) -> f1.getDepartureTime().compareTo(f2.getDepartureTime()))
                .toList();

        return flights.stream()
                .map(this::convertToFlightResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FlightResponseDTO getFlightDetail(String id) {
        Flight flight = flightRepository.findById(id).orElse(null);
        if (flight == null) {
            return null;
        }
        return convertToFlightDetailResponseDTO(flight);
    }

    @Override
    @Transactional
    public FlightResponseDTO updateFlight(UpdateFlightRequestDTO dto) {
        Flight flight = flightRepository.findById(dto.getId()).orElse(null);
        if (flight == null || flight.getIsDeleted()) {
            return null;
        }

        // Check if flight can be updated (only Scheduled or Delayed)
        if (flight.getStatus() != 1 && flight.getStatus() != 4) {
            throw new IllegalStateException("Cannot update flight that is not scheduled or delayed");
        }

        // Validate departure < arrival
        if (dto.getDepartureTime().isAfter(dto.getArrivalTime()) || dto.getDepartureTime().equals(dto.getArrivalTime())) {
            throw new IllegalArgumentException("Departure time must be before arrival time");
        }

        // Validate departure time is not in the past
        if (dto.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Departure time cannot be in the past");
        }

        // Check for overlapping flights (excluding current flight)
        List<Flight> overlappingFlights = flightRepository.findOverlappingFlightsExcludingId(
                flight.getAirplaneId(), dto.getId(), dto.getDepartureTime(), dto.getArrivalTime());
        if (!overlappingFlights.isEmpty()) {
            throw new IllegalArgumentException("Airplane is already scheduled for overlapping flights");
        }

        // Update flight
        flight = flight.toBuilder()
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .terminal(dto.getTerminal())
                .gate(dto.getGate())
                .baggageAllowance(dto.getBaggageAllowance())
                .facilities(dto.getFacilities())
                .build();

        // Check if status should change to Delayed
        if (dto.getDepartureTime().isAfter(flight.getDepartureTime())) {
            flight.setStatus(4); // Delayed
        }

        flight = flightRepository.save(flight);

        // Update classes if provided
        if (dto.getClasses() != null && !dto.getClasses().isEmpty()) {
            // This would be more complex in a real implementation
            // For now, we'll skip updating classes as per the requirements focus
        }

        return convertToFlightResponseDTO(flight);
    }

    @Override
    @Transactional
    public FlightResponseDTO deleteFlight(String id) {
        Flight flight = flightRepository.findById(id).orElse(null);
        if (flight == null) {
            return null;
        }

        if (flight.getIsDeleted()) {
            throw new IllegalStateException("Flight is already deleted");
        }

        // Check if flight can be deleted (only Scheduled or Delayed)
        if (flight.getStatus() != 1 && flight.getStatus() != 4) {
            throw new IllegalStateException("Cannot delete flight that is in flight or finished");
        }

        // TODO: Check for active bookings when booking is implemented
        // For now, assume no bookings

        flight.setIsDeleted(true);
        flight.setStatus(5); // Cancelled
        flight = flightRepository.save(flight);

        return convertToFlightResponseDTO(flight);
    }

    private String generateFlightId(String airplaneId) {
        Integer maxNumber = flightRepository.findMaxFlightNumberByAirplaneId(airplaneId);
        int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;
        return String.format("%s-%03d", airplaneId, nextNumber);
    }

    private void generateSeatsForClass(Integer classFlightId, Integer seatCapacity, String classType) {
        String prefix = getClassPrefix(classType);

        for (int i = 1; i <= seatCapacity; i++) {
            AddSeatRequestDTO seatDto = AddSeatRequestDTO.builder()
                    .classFlightId(classFlightId)
                    .seatCode(String.format("%s%03d", prefix, i))
                    .build();

            seatRestService.createSeat(seatDto);
        }
    }

    private String getClassPrefix(String classType) {
        switch (classType.toLowerCase()) {
            case "economy": return "EC";
            case "business": return "BU";
            case "first": return "FI";
            default: return "EC";
        }
    }

    private FlightResponseDTO convertToFlightResponseDTO(Flight flight) {
        // Get class flights for this flight
        List<ClassFlightResponseDTO> classFlights = classFlightRestService.getClassFlightsByFlight(flight.getId());

        return FlightResponseDTO.builder()
                .id(flight.getId())
                .airlineId(flight.getAirlineId())
                .airplaneId(flight.getAirplaneId())
                .originAirportCode(flight.getOriginAirportCode())
                .destinationAirportCode(flight.getDestinationAirportCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .terminal(flight.getTerminal())
                .gate(flight.getGate())
                .baggageAllowance(flight.getBaggageAllowance())
                .facilities(flight.getFacilities())
                .status(flight.getStatus())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .isDeleted(flight.getIsDeleted())
                .classes(classFlights)
                .build();
    }

    private FlightResponseDTO convertToFlightDetailResponseDTO(Flight flight) {
        // Get class flights with seats for this flight (detailed view)
        List<ClassFlightResponseDTO> classFlights = classFlightRestService.getClassFlightsByFlight(flight.getId())
                .stream()
                .map(cf -> classFlightRestService.getClassFlightDetail(cf.getId()))
                .toList();

        return FlightResponseDTO.builder()
                .id(flight.getId())
                .airlineId(flight.getAirlineId())
                .airplaneId(flight.getAirplaneId())
                .originAirportCode(flight.getOriginAirportCode())
                .destinationAirportCode(flight.getDestinationAirportCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .terminal(flight.getTerminal())
                .gate(flight.getGate())
                .baggageAllowance(flight.getBaggageAllowance())
                .facilities(flight.getFacilities())
                .status(flight.getStatus())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .isDeleted(flight.getIsDeleted())
                .classes(classFlights)
                .build();
    }
}
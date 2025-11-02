package apap.ti._5.flight_2306211660_be.restservice.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.flight_2306211660_be.model.Booking;
import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Flight;
import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;

@Service
public class BookingRestServiceImpl implements BookingRestService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingPassengerRepository bookingPassengerRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private ClassFlightRepository classFlightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Override
    @Transactional
    public BookingResponseDTO createBooking(AddBookingRequestDTO dto) {
        // Validate flight exists and is active
        Flight flight = flightRepository.findById(dto.getFlightId()).orElse(null);
        if (flight == null || flight.getIsDeleted()) {
            throw new IllegalArgumentException("Flight not found or not active");
        }

        // Check flight status (only allow booking for Scheduled flights)
        if (flight.getStatus() != 1) { // 1 = Scheduled
            throw new IllegalArgumentException("Cannot book flight that is not scheduled");
        }

        // Validate class flight exists
        ClassFlight classFlight = classFlightRepository.findById(dto.getClassFlightId()).orElse(null);
        if (classFlight == null) {
            throw new IllegalArgumentException("Class flight not found");
        }

        // Check if enough seats are available
        if (classFlight.getAvailableSeats() < dto.getPassengerCount()) {
            throw new IllegalArgumentException("Not enough seats available in this class");
        }

        // Validate seat IDs if provided
        if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
            // Check if all seat IDs belong to the specified class flight
            for (Integer seatId : dto.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId).orElse(null);
                if (seat == null) {
                    throw new IllegalArgumentException("Seat with ID " + seatId + " does not exist");
                }
                if (!seat.getClassFlightId().equals(dto.getClassFlightId())) {
                    throw new IllegalArgumentException("Seat with ID " + seatId + " does not belong to the specified class flight");
                }
                if (seat.getIsBooked()) {
                    throw new IllegalArgumentException("Seat with ID " + seatId + " is already booked");
                }
            }

            // Check if number of seat IDs matches passenger count
            if (dto.getSeatIds().size() != dto.getPassengerCount()) {
                throw new IllegalArgumentException("Number of seat IDs must match passenger count");
            }
        }

        // Validate passengers count matches
        if (dto.getPassengers().size() != dto.getPassengerCount()) {
            throw new IllegalArgumentException("Number of passengers does not match passenger count");
        }

        // Generate booking code
        String bookingCode = generateBookingCode(dto.getFlightId());

        // Calculate total price
        BigDecimal totalPrice = classFlight.getPrice().multiply(BigDecimal.valueOf(dto.getPassengerCount()));

        // Create booking
        Booking booking = Booking.builder()
                .id(bookingCode)
                .flightId(dto.getFlightId())
                .classFlightId(dto.getClassFlightId())
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .passengerCount(dto.getPassengerCount())
                .status(1) // Unpaid
                .totalPrice(totalPrice)
                .isDeleted(false)
                .build();

        booking = bookingRepository.save(booking);

        // Create passengers and booking-passenger relationships
        for (AddPassengerRequestDTO passengerDto : dto.getPassengers()) {
            // Check if passenger already exists by ID passport
            Passenger passenger;
            if (passengerRepository.existsByIdPassport(passengerDto.getIdPassport())) {
                // Use existing passenger
                passenger = passengerRepository.findByIdPassport(passengerDto.getIdPassport());
            } else {
                // Create new passenger
                passenger = Passenger.builder()
                        .id(java.util.UUID.randomUUID())
                        .fullName(passengerDto.getFullName())
                        .birthDate(passengerDto.getBirthDate())
                        .gender(passengerDto.getGender())
                        .idPassport(passengerDto.getIdPassport())
                        .build();

                passenger = passengerRepository.save(passenger);
            }

            // Create booking-passenger relationship
            BookingPassenger bookingPassenger = BookingPassenger.builder()
                    .bookingId(bookingCode)
                    .passengerId(passenger.getId())
                    .build();

            bookingPassengerRepository.save(bookingPassenger);
        }

        // Allocate seats if specified, assigning passengers to specific seats
        if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
            allocateSeatsToPassengers(bookingCode, dto.getSeatIds());
        } else {
            // If no specific seats requested, just mark seats as booked (no passenger assignment)
            allocateSeats(bookingCode, dto.getPassengerCount(), classFlight.getId());
        }

        // Update available seats
        classFlight.setAvailableSeats(classFlight.getAvailableSeats() - dto.getPassengerCount());
        classFlightRepository.save(classFlight);

        return convertToBookingResponseDTO(booking);
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findByIsDeleted(false);
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getBookingsByFlight(String flightId) {
        List<Booking> bookings = bookingRepository.findByFlightIdAndIsDeleted(flightId, false);
        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO getBooking(String id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || booking.getIsDeleted()) {
            return null;
        }
        return convertToBookingResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBooking(UpdateBookingRequestDTO dto) {
        Booking booking = bookingRepository.findById(dto.getId()).orElse(null);
        if (booking == null || booking.getIsDeleted()) {
            return null;
        }

        // Only allow updates for unpaid bookings
        if (booking.getStatus() != 1) {
            throw new IllegalStateException("Cannot update booking that is not unpaid");
        }

        // Update contact information
        booking = booking.toBuilder()
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .build();

        booking = bookingRepository.save(booking);

        // Update passengers if provided
        if (dto.getPassengers() != null && !dto.getPassengers().isEmpty()) {
            // This would be more complex - for now, we'll skip passenger updates
        }

        // Update seat allocations if provided
        if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
            // Deallocate old seats and allocate new ones
            deallocateSeats(dto.getId());
            allocateSeats(dto.getId(), dto.getSeatIds());
        }

        return convertToBookingResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO deleteBooking(String id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return null;
        }

        if (booking.getIsDeleted()) {
            throw new IllegalStateException("Booking is already deleted");
        }

        // Only allow deletion for unpaid bookings
        if (booking.getStatus() != 1) {
            throw new IllegalStateException("Cannot delete booking that is paid or cancelled");
        }

        // Deallocate seats
        deallocateSeats(id);

        // Restore available seats
        ClassFlight classFlight = classFlightRepository.findById(booking.getClassFlightId()).orElse(null);
        if (classFlight != null) {
            classFlight.setAvailableSeats(classFlight.getAvailableSeats() + booking.getPassengerCount());
            classFlightRepository.save(classFlight);
        }

        // Soft delete booking
        booking.setIsDeleted(true);
        booking.setStatus(3); // Cancelled
        booking = bookingRepository.save(booking);

        return convertToBookingResponseDTO(booking);
    }

    private String generateBookingCode(String flightId) {
        String prefix = flightId + "-" +
                       flightRepository.findById(flightId).get().getOriginAirportCode() + "-" +
                       flightRepository.findById(flightId).get().getDestinationAirportCode();

        Integer maxNumber = bookingRepository.findMaxBookingNumberByPrefix(prefix + "-");
        int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

        return String.format("%s-%03d", prefix, nextNumber);
    }

    private void allocateSeats(String bookingId, List<Integer> seatIds) {
        for (Integer seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null && !seat.getIsBooked()) {
                seat.setIsBooked(true);
                seatRepository.save(seat);
            }
        }
    }

    private void allocateSeats(String bookingId, Integer passengerCount, Integer classFlightId) {
        // Find available seats in the class flight
        List<Seat> availableSeats = seatRepository.findAll().stream()
                .filter(seat -> seat.getClassFlightId().equals(classFlightId) && !seat.getIsBooked())
                .limit(passengerCount)
                .toList();

        // Mark seats as booked
        for (Seat seat : availableSeats) {
            seat.setIsBooked(true);
            seatRepository.save(seat);
        }
    }

    private void allocateSeatsToPassengers(String bookingId, List<Integer> seatIds) {
        // Get the passengers that were created for this booking
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findByBookingId(bookingId);

        for (int i = 0; i < seatIds.size() && i < bookingPassengers.size(); i++) {
            Integer seatId = seatIds.get(i);
            UUID passengerId = bookingPassengers.get(i).getPassengerId();

            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null && !seat.getIsBooked()) {
                seat.setIsBooked(true);
                seat.setPassengerId(passengerId);
                seatRepository.save(seat);
            }
        }
    }

    private void deallocateSeats(String bookingId) {
        // Find all seats allocated to passengers in this booking
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findByBookingId(bookingId);

        for (BookingPassenger bp : bookingPassengers) {
            // Find seats assigned to this passenger
            List<Seat> passengerSeats = seatRepository.findAll().stream()
                    .filter(seat -> bp.getPassengerId().equals(seat.getPassengerId()))
                    .toList();

            // Deallocate seats
            for (Seat seat : passengerSeats) {
                seat.setIsBooked(false);
                seat.setPassengerId(null);
                seatRepository.save(seat);
            }
        }
    }

    private BookingResponseDTO convertToBookingResponseDTO(Booking booking) {
        // Get passengers for this booking through BookingPassenger junction table
        List<PassengerResponseDTO> passengers = getPassengersForBooking(booking.getId());

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .flightId(booking.getFlightId())
                .classFlightId(booking.getClassFlightId())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .passengerCount(booking.getPassengerCount())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .isDeleted(booking.getIsDeleted())
                .passengers(passengers)
                .build();
    }

    private List<PassengerResponseDTO> getPassengersForBooking(String bookingId) {
        // Get booking-passenger relationships
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findByBookingId(bookingId);

        // Convert to passenger response DTOs
        return bookingPassengers.stream()
                .map(bp -> {
                    Passenger passenger = passengerRepository.findById(bp.getPassengerId()).orElse(null);
                    if (passenger != null) {
                        return PassengerResponseDTO.builder()
                                .id(passenger.getId())
                                .fullName(passenger.getFullName())
                                .birthDate(passenger.getBirthDate())
                                .gender(passenger.getGender())
                                .idPassport(passenger.getIdPassport())
                                .createdAt(passenger.getCreatedAt())
                                .updatedAt(passenger.getUpdatedAt())
                                .build();
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
}

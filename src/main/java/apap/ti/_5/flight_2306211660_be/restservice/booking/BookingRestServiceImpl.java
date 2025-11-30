package apap.ti._5.flight_2306211660_be.restservice.booking;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.flight_2306211660_be.model.Booking;
import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Flight;
import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.AirlineRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResultDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartSummaryDTO;
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
 
    @Autowired
    private AirlineRepository airlineRepository;

    private static final Logger logger = LoggerFactory.getLogger(BookingRestServiceImpl.class);

    @Override
    @Transactional
    public BookingResponseDTO createBooking(AddBookingRequestDTO dto) {
        // Validate flight exists and is active
        Flight flight = flightRepository.findById(dto.getFlightId()).orElse(null);
        if (flight == null || flight.getIsDeleted()) {
            throw new IllegalArgumentException("Flight not found or not active");
        }

        // Check flight status
        // Allow booking for Scheduled (1) and Delayed (4). Disallow others (e.g., In Flight (2), Finished (3), Cancelled (5)).
        if (flight.getStatus() != 1 && flight.getStatus() != 4) {
            throw new IllegalArgumentException("Cannot book flight that is not scheduled or delayed");
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

            // Validate passengerCount limits
            if (dto.getPassengerCount() == null || dto.getPassengerCount() <= 0) {
                throw new IllegalArgumentException("Passenger count must be greater than zero");
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

        // Update available seats safely
        int newAvailableFinal = classFlight.getAvailableSeats() - dto.getPassengerCount();
        if (newAvailableFinal < 0) throw new IllegalStateException("Available seats would become negative");
        classFlight.setAvailableSeats(newAvailableFinal);
        classFlightRepository.save(classFlight);

        // TODO: call billing service to create bill for this booking (async/remote)

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
    public List<BookingResponseDTO> getAllBookings(Boolean includeDeleted, String search, String contactEmail, Integer status) {
        List<Booking> bookings;
        if (includeDeleted != null && includeDeleted) {
            bookings = bookingRepository.findAll();
        } else {
            bookings = bookingRepository.findByIsDeleted(false);
        }

        // Apply search filter: booking id or flight id (case-insensitive)
        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim().toLowerCase();
            bookings = bookings.stream()
                    .filter(b -> (b.getId() != null && b.getId().toLowerCase().contains(s)) || (b.getFlightId() != null && b.getFlightId().toLowerCase().contains(s)))
                    .collect(Collectors.toList());
        }

        // Apply contactEmail filter (case-sensitive exact match)
        if (contactEmail != null && !contactEmail.isEmpty()) {
            bookings = bookings.stream()
                    .filter(b -> contactEmail.equals(b.getContactEmail()))
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (status != null) {
            bookings = bookings.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getAllBookings(Boolean includeDeleted) {
        List<Booking> bookings;
        if (includeDeleted != null && includeDeleted) {
            bookings = bookingRepository.findAll();
        } else {
            bookings = bookingRepository.findByIsDeleted(false);
        }
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
    public List<BookingResponseDTO> getBookingsByFlight(String flightId, Boolean includeDeleted) {
        return getBookingsByFlight(flightId, includeDeleted, null, null, null);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByFlight(String flightId, Boolean includeDeleted, String search, String contactEmail, Integer status) {
        List<Booking> bookings;
        if (includeDeleted != null && includeDeleted) {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> flightId.equals(b.getFlightId()))
                    .collect(Collectors.toList());
        } else {
            bookings = bookingRepository.findByFlightIdAndIsDeleted(flightId, false);
        }

        // Apply search filter (booking id or flight id)
        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim().toLowerCase();
            bookings = bookings.stream()
                    .filter(b -> (b.getId() != null && b.getId().toLowerCase().contains(s)) || (b.getFlightId() != null && b.getFlightId().toLowerCase().contains(s)))
                    .collect(Collectors.toList());
        }

        if (contactEmail != null && !contactEmail.isEmpty()) {
            bookings = bookings.stream()
                    .filter(b -> contactEmail.equals(b.getContactEmail()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            bookings = bookings.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        return bookings.stream()
                .map(this::convertToBookingResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO getBooking(String id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return null;
        }
        // Only return if not soft-deleted
        if (booking.getIsDeleted() != null && booking.getIsDeleted()) return null;
        return convertToBookingResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBooking(UpdateBookingRequestDTO dto) {
        Booking booking = bookingRepository.findById(dto.getId()).orElse(null);
        if (booking == null || booking.getIsDeleted()) {
            return null;
        }

        // Only allow updates for Unpaid (1) or Paid (2)
        if (booking.getStatus() != 1 && booking.getStatus() != 2) {
            throw new IllegalStateException("Only unpaid or paid bookings can be updated");
        }

        // Validate related flight status: must be Scheduled (1) or Delayed (4)
        Flight relatedFlight = flightRepository.findById(booking.getFlightId()).orElse(null);
        if (relatedFlight == null || relatedFlight.getIsDeleted() || (relatedFlight.getStatus() != 1 && relatedFlight.getStatus() != 4)) {
            throw new IllegalStateException("Cannot update booking because flight is not scheduled or delayed");
        }

        // Update contact information
        booking = booking.toBuilder()
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .build();

        booking = bookingRepository.save(booking);

        // Update passengers if provided (add/update/remove) and recompute counts and pricing
        {
            // Current relations
            List<BookingPassenger> currentBps = bookingPassengerRepository.findByBookingId(dto.getId());

            java.util.Set<java.util.UUID> keepIds = new java.util.HashSet<>();
            if (dto.getPassengers() != null) {
                for (apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO upd : dto.getPassengers()) {
                    Passenger p = passengerRepository.findById(upd.getId()).orElse(null);
                    if (p == null) {
                        throw new IllegalArgumentException("Passenger to update not found: " + upd.getId());
                    }
                    p = p.toBuilder()
                            .fullName(upd.getFullName())
                            .birthDate(upd.getBirthDate())
                            .gender(upd.getGender())
                            .idPassport(upd.getIdPassport())
                            .build();
                    passengerRepository.save(p);
                    keepIds.add(upd.getId());
                }
            }

            // Remove booking-passenger relations not in keepIds (only when passengers list provided)
            if (dto.getPassengers() != null) {
                for (BookingPassenger bp : currentBps) {
                    if (!keepIds.contains(bp.getPassengerId())) {
                        // Deallocate any seat tied to this passenger
                        List<Seat> seatsOfPassenger = seatRepository.findAll().stream()
                                .filter(s -> bp.getPassengerId().equals(s.getPassengerId()))
                                .toList();
                        for (Seat s : seatsOfPassenger) {
                            s.setIsBooked(false);
                            s.setPassengerId(null);
                            seatRepository.save(s);
                        }
                        bookingPassengerRepository.delete(bp);
                    }
                }
            }

            // Add new passengers if provided
            if (dto.getNewPassengers() != null && !dto.getNewPassengers().isEmpty()) {
                for (AddPassengerRequestDTO addP : dto.getNewPassengers()) {
                    Passenger passenger;
                    if (passengerRepository.existsByIdPassport(addP.getIdPassport())) {
                        passenger = passengerRepository.findByIdPassport(addP.getIdPassport());
                    } else {
                        passenger = Passenger.builder()
                                .id(java.util.UUID.randomUUID())
                                .fullName(addP.getFullName())
                                .birthDate(addP.getBirthDate())
                                .gender(addP.getGender())
                                .idPassport(addP.getIdPassport())
                                .build();
                        passenger = passengerRepository.save(passenger);
                    }

                    BookingPassenger newBp = BookingPassenger.builder()
                            .bookingId(booking.getId())
                            .passengerId(passenger.getId())
                            .build();
                    bookingPassengerRepository.save(newBp);
                }
            }

            // Recompute passenger count and adjust availability and pricing
            int oldCount = booking.getPassengerCount();
            int newCount = bookingPassengerRepository.findByBookingId(booking.getId()).size();
            if (newCount != oldCount) {
                ClassFlight cf = classFlightRepository.findById(booking.getClassFlightId()).orElse(null);
                if (cf == null) throw new IllegalStateException("Class flight not found for booking");
                int delta = newCount - oldCount;
                if (delta > 0) {
                    if (cf.getAvailableSeats() < delta) {
                        throw new IllegalArgumentException("Not enough seats available for additional passengers");
                    }
                    cf.setAvailableSeats(cf.getAvailableSeats() - delta);
                } else if (delta < 0) {
                    cf.setAvailableSeats(cf.getAvailableSeats() + (-delta));
                }
                classFlightRepository.save(cf);
                booking = booking.toBuilder()
                        .passengerCount(newCount)
                        .totalPrice(cf.getPrice().multiply(java.math.BigDecimal.valueOf(newCount)))
                        .build();
                booking = bookingRepository.save(booking);
            } else {
                // Keep pricing consistent with current class price
                ClassFlight cf = classFlightRepository.findById(booking.getClassFlightId()).orElse(null);
                if (cf != null) {
                    booking = booking.toBuilder()
                            .totalPrice(cf.getPrice().multiply(java.math.BigDecimal.valueOf(booking.getPassengerCount())))
                            .build();
                    booking = bookingRepository.save(booking);
                }
            }
        }

        // Update seat allocations if provided, else ensure new passengers have seats
        if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
            // Validate provided seats length matches current passengerCount
            if (dto.getSeatIds().size() != booking.getPassengerCount()) {
                throw new IllegalArgumentException("Number of seat IDs must match current passenger count");
            }

            // Validate distinct seat IDs
            java.util.Set<Integer> distinctSeatIds = new java.util.HashSet<>(dto.getSeatIds());
            if (distinctSeatIds.size() != dto.getSeatIds().size()) {
                throw new IllegalArgumentException("Duplicate seat IDs provided");
            }

            // Build current passenger id set for this booking (after add/remove above)
            java.util.List<BookingPassenger> allBps = bookingPassengerRepository.findByBookingId(booking.getId());
            java.util.Set<java.util.UUID> bpIds = allBps.stream()
                    .map(BookingPassenger::getPassengerId)
                    .collect(java.util.stream.Collectors.toSet());

            // Validate each requested seat:
            // - belongs to the same classFlight
            // - either unbooked OR currently booked by a passenger of this booking (allow remap)
            for (Integer seatId : dto.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId).orElse(null);
                if (seat == null) {
                    throw new IllegalArgumentException("Seat with ID " + seatId + " does not exist");
                }
                if (!seat.getClassFlightId().equals(booking.getClassFlightId())) {
                    throw new IllegalArgumentException("Seat with ID " + seatId + " does not belong to the booking's class flight");
                }
                if (Boolean.TRUE.equals(seat.getIsBooked())) {
                    if (seat.getPassengerId() == null || !bpIds.contains(seat.getPassengerId())) {
                        throw new IllegalArgumentException("Seat with ID " + seatId + " is already booked");
                    }
                }
            }

            // Deallocate old seats and allocate new ones (preserve passenger mapping order)
            deallocateSeats(dto.getId());
            allocateSeatsToPassengers(dto.getId(), dto.getSeatIds());
        } else {
            // If passenger count increased or some passengers have no seats, allocate missing seats automatically
            java.util.List<BookingPassenger> allBps = bookingPassengerRepository.findByBookingId(booking.getId());
            java.util.Set<java.util.UUID> bpIds = allBps.stream()
                    .map(BookingPassenger::getPassengerId)
                    .collect(java.util.stream.Collectors.toSet());

            final Integer classFlightIdForFilter = booking.getClassFlightId();
            long bookedSeatsForBooking = seatRepository.findAll().stream()
                    .filter(s -> s.getClassFlightId().equals(classFlightIdForFilter)
                            && s.getPassengerId() != null
                            && bpIds.contains(s.getPassengerId()))
                    .count();

            int missing = booking.getPassengerCount() - (int) bookedSeatsForBooking;
            if (missing > 0) {
                allocateSeats(booking.getId(), missing, booking.getClassFlightId());
            }
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

        // Only allow cancel for Unpaid (1) or Paid (2)
        if (booking.getStatus() != 1 && booking.getStatus() != 2) {
            throw new IllegalStateException("Only unpaid or paid bookings can be cancelled");
        }

        // Validate related flight status: must be Scheduled (1) or Delayed (4)
        Flight relatedFlight = flightRepository.findById(booking.getFlightId()).orElse(null);
        if (relatedFlight == null || (relatedFlight.getStatus() != 1 && relatedFlight.getStatus() != 4)) {
            throw new IllegalStateException("Cannot cancel booking because flight is not scheduled or delayed");
        }
        // TODO: refund handling for Paid status (booking.status == 2)

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

    @Override
    public long getTodayBookings() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return bookingRepository.findByIsDeleted(false).stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().toLocalDate().isEqual(today))
                .count();
    }

    private String generateBookingCode(String flightId) {
        String prefix = flightId + "-" +
                       flightRepository.findById(flightId).get().getOriginAirportCode() + "-" +
                       flightRepository.findById(flightId).get().getDestinationAirportCode();

        Integer maxNumber = bookingRepository.findMaxBookingNumberByPrefix(prefix + "-");
        int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

        return String.format("%s-%03d", prefix, nextNumber);
    }

    private void allocateSeats(String bookingId, Integer passengerCount, Integer classFlightId) {
        // Find available seats in the class flight (stable order)
        List<Seat> availableSeats = seatRepository.findAll().stream()
                .filter(seat -> seat.getClassFlightId().equals(classFlightId) && !seat.getIsBooked())
                .sorted(java.util.Comparator.comparing(Seat::getSeatCode))
                .limit(passengerCount)
                .toList();

        // Map seats to passengers in deterministic order for this booking
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findByBookingId(bookingId);

        for (int i = 0; i < availableSeats.size(); i++) {
            Seat seat = availableSeats.get(i);
            seat.setIsBooked(true);
            if (i < bookingPassengers.size()) {
                seat.setPassengerId(bookingPassengers.get(i).getPassengerId());
            }
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

        // Resolve class type for readability
        ClassFlight cf = classFlightRepository.findById(booking.getClassFlightId()).orElse(null);
        String classType = (cf != null) ? cf.getClassType() : null;

        // Build seat assignments for booking detail display
        List<apap.ti._5.flight_2306211660_be.restdto.response.booking.PassengerSeatAssignmentResponseDTO> seatAssignments =
                getSeatAssignmentsForBooking(booking.getId(), booking.getClassFlightId());

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .flightId(booking.getFlightId())
            .route((flightRepository.findById(booking.getFlightId()).isPresent()) ?
                (flightRepository.findById(booking.getFlightId()).get().getOriginAirportCode() + "-" + flightRepository.findById(booking.getFlightId()).get().getDestinationAirportCode()) : null)
                .classFlightId(booking.getClassFlightId())
                .classType(classType)
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .passengerCount(booking.getPassengerCount())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .isDeleted(booking.getIsDeleted())
                .passengers(passengers)
                .seatAssignments(seatAssignments)
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

    private List<apap.ti._5.flight_2306211660_be.restdto.response.booking.PassengerSeatAssignmentResponseDTO> getSeatAssignmentsForBooking(String bookingId, Integer classFlightId) {
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findByBookingId(bookingId);
        List<Seat> seats = seatRepository.findAll().stream()
                .filter(seat -> seat.getClassFlightId().equals(classFlightId) && seat.getIsBooked() && seat.getPassengerId() != null)
                .toList();

        return bookingPassengers.stream()
                .map(bp -> {
                    var seatOpt = seats.stream()
                            .filter(s -> bp.getPassengerId().equals(s.getPassengerId()))
                            .findFirst();
                    if (seatOpt.isPresent()) {
                        Seat s = seatOpt.get();
                        Passenger p = passengerRepository.findById(bp.getPassengerId()).orElse(null);
                        String pname = (p != null) ? p.getFullName() : null;
                        return new apap.ti._5.flight_2306211660_be.restdto.response.booking.PassengerSeatAssignmentResponseDTO(
                                bp.getPassengerId(), s.getId(), s.getSeatCode(), pname
                        );
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
 
    @Override
    @Transactional
    public BookingResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto) {
        String bookingId = dto.getServiceReferenceId();
        
        // Fetch booking and update status to Paid (2)
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(2); // Paid
            booking = bookingRepository.save(booking);
            logger.info("Booking {} status updated to Paid", bookingId);
        }
        
        return booking != null ? convertToBookingResponseDTO(booking) : null;
    }

    @Override
    public java.util.List<BookingChartResponseDTO> getBookingChart(int month, int year) {
        // Filter only Unpaid(1) and Paid(2), not soft-deleted, within given month/year by createdAt
        List<Booking> filtered = bookingRepository.findAll().stream()
                .filter(b -> b.getIsDeleted() != null && !b.getIsDeleted())
                .filter(b -> b.getStatus() != null && (b.getStatus() == 1 || b.getStatus() == 2))
                .filter(b -> {
                    if (b.getCreatedAt() == null) return false;
                    java.time.LocalDateTime c = b.getCreatedAt();
                    return c.getMonthValue() == month && c.getYear() == year;
                })
                .collect(Collectors.toList());

        // Group by flightId and compute bookingCount + totalRevenue
        List<BookingChartResponseDTO> stats = filtered.stream()
                .collect(Collectors.groupingBy(Booking::getFlightId))
                .entrySet().stream()
                .map(e -> {
                    String flightId = e.getKey();
                    List<Booking> list = e.getValue();
                    Long bookingCount = (long) list.size();
                    BigDecimal totalRevenue = list.stream()
                            .map(Booking::getTotalPrice)
                            .filter(v -> v != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Flight f = flightRepository.findById(flightId).orElse(null);
                    String origin = (f != null) ? f.getOriginAirportCode() : null;
                    String destination = (f != null) ? f.getDestinationAirportCode() : null;
                    String airlineName = null;
                    if (f != null && f.getAirlineId() != null) {
                        var a = airlineRepository.findById(f.getAirlineId()).orElse(null);
                        if (a != null) airlineName = a.getName();
                    }

                    return BookingChartResponseDTO.builder()
                            .flightId(flightId)
                            .airlineName(airlineName)
                            .origin(origin)
                            .destination(destination)
                            .totalBookings(bookingCount)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .sorted(java.util.Comparator.comparing(BookingChartResponseDTO::getFlightId))
                .collect(Collectors.toList());

        return stats;
    }

            @Override
            public BookingChartResultDTO getBookingChartData(int month, int year) {
            List<BookingChartResponseDTO> chart = getBookingChart(month, year);

            // compute summary
            long totalBookings = chart.stream().mapToLong(c -> c.getTotalBookings() != null ? c.getTotalBookings().longValue() : 0L).sum();
            java.math.BigDecimal totalRevenue = chart.stream()
                .map(c -> c.getTotalRevenue() != null ? c.getTotalRevenue() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            BookingChartResponseDTO top = chart.stream()
                .max(Comparator.comparingLong((BookingChartResponseDTO c) -> c.getTotalBookings() != null ? c.getTotalBookings().longValue() : 0L)
                    .thenComparing(c -> c.getTotalRevenue() != null ? c.getTotalRevenue() : java.math.BigDecimal.ZERO))
                .orElse(null);

            BookingChartSummaryDTO summary = new BookingChartSummaryDTO(
                totalBookings,
                totalRevenue,
                top != null ? top.getFlightId() : null
            );

            return new BookingChartResultDTO(chart, summary);
            }
}

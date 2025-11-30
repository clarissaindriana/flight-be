package apap.ti._5.flight_2306211660_be.restservice;

import apap.ti._5.flight_2306211660_be.model.*;
import apap.ti._5.flight_2306211660_be.repository.AirlineRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.booking.BookingRestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * High coverage unit tests for
 * [BookingRestServiceImpl](flight-2306211660-be/src/main/java/apap/ti/_5/flight_2306211660_be/restservice/booking/BookingRestServiceImpl.java:1)
 */
@ExtendWith(MockitoExtension.class)
public class BookingRestServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingPassengerRepository bookingPassengerRepository;
    @Mock private PassengerRepository passengerRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private ClassFlightRepository classFlightRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private AirlineRepository airlineRepository;

    @InjectMocks
    private BookingRestServiceImpl service;

    private Flight flight(String id, String origin, String dest, int status, boolean deleted) {
        return Flight.builder()
                .id(id)
                .originAirportCode(origin)
                .destinationAirportCode(dest)
                .status(status)
                .isDeleted(deleted)
                .build();
    }

    private ClassFlight classFlight(Integer id, String classType, int cap, int available, String price) {
        return ClassFlight.builder()
                .id(id)
                .classType(classType)
                .seatCapacity(cap)
                .availableSeats(available)
                .price(new BigDecimal(price))
                .flightId("FL-1")
                .build();
    }

    private Seat seat(Integer id, Integer classFlightId, String code, boolean booked, UUID passengerId) {
        return Seat.builder()
                .id(id)
                .classFlightId(classFlightId)
                .seatCode(code)
                .isBooked(booked)
                .passengerId(passengerId)
                .build();
    }

    private Booking booking(String id, String flightId, int classFlightId, int passengerCount, int status, boolean deleted) {
        return Booking.builder()
                .id(id)
                .flightId(flightId)
                .classFlightId(classFlightId)
                .passengerCount(passengerCount)
                .status(status)
                .isDeleted(deleted)
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    // -------------------- createBooking --------------------

    @Test
    @DisplayName("createBooking: flight not found or deleted -> IllegalArgumentException")
    void createBooking_flightInvalid() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-404").classFlightId(10).passengerCount(1)
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Flight not found or not active"));
    }

    @Test
    @DisplayName("createBooking: flight status not allowed -> IllegalArgumentException")
    void createBooking_statusNotAllowed() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(1)
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 2, false))); // In Flight

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("not scheduled or delayed"));
    }

    @Test
    @DisplayName("createBooking: class flight not found -> IllegalArgumentException")
    void createBooking_classFlightNotFound() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(1)
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Class flight not found"));
    }

    @Test
    @DisplayName("createBooking: not enough seats -> IllegalArgumentException")
    void createBooking_notEnoughSeats() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(3)
                .passengers(List.of(
                        AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build(),
                        AddPassengerRequestDTO.builder().idPassport("P2").fullName("B").build(),
                        AddPassengerRequestDTO.builder().idPassport("P3").fullName("C").build()
                ))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 2, 2, "1000000")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Not enough seats"));
    }

    @Test
    @DisplayName("createBooking: passenger count <=0 -> IllegalArgumentException")
    void createBooking_passengerCountZero() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(0)
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Passenger count must be greater than zero"));
    }

    @Test
    @DisplayName("createBooking: seatIds size mismatch -> IllegalArgumentException")
    void createBooking_seatIdsMismatch() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(2)
                .seatIds(List.of(1))
                .passengers(List.of(
                        AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build(),
                        AddPassengerRequestDTO.builder().idPassport("P2").fullName("B").build()
                ))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));
        // Ensure seat exists and matches class so we reach the size-mismatch validation branch
        when(seatRepository.findById(1)).thenReturn(Optional.of(seat(1, 10, "EC001", false, null)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Number of seat IDs must match passenger count"));
    }

    @Test
    @DisplayName("createBooking: passenger count mismatch with passengers list -> IllegalArgumentException")
    void createBooking_passengerCountMismatch() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(2)
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Number of passengers does not match passenger count"));
    }

    @Test
    @DisplayName("createBooking: seat not found -> IllegalArgumentException")
    void createBooking_seatNotFound() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(1)
                .seatIds(List.of(999))
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));
        when(seatRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Seat with ID 999 does not exist"));
    }

    @Test
    @DisplayName("createBooking: seat already booked -> IllegalArgumentException")
    void createBooking_seatBooked() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(1)
                .seatIds(List.of(1))
                .passengers(List.of(AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build()))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));
        when(seatRepository.findById(1)).thenReturn(Optional.of(seat(1, 10, "EC001", true, UUID.randomUUID())));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createBooking(dto));
        assertTrue(ex.getMessage().contains("Seat with ID 1 is already booked"));
    }

    @Test
    @DisplayName("createBooking: success with auto seat allocation")
    void createBooking_success_autoAllocate() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(2)
                .contactEmail("e@x.com").contactPhone("08123")
                .passengers(List.of(
                        AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build(),
                        AddPassengerRequestDTO.builder().idPassport("P2").fullName("B").build()
                ))
                .build();

        // Flight and classFlight valid
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));

        // Booking code generation needs flight twice and max booking number
        when(bookingRepository.findMaxBookingNumberByPrefix("FL-1-CGK-DPS-")).thenReturn(null);
        // Flight lookups inside generateBookingCode
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));

        // Save booking returns assigned id/code
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId("FL-1-CGK-DPS-001");
            return b;
        });

        // Passengers: none pre-exist
        when(passengerRepository.existsByIdPassport(anyString())).thenReturn(false);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        // Booking-passengers created; later lookup for allocation
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        when(bookingPassengerRepository.findByBookingId("FL-1-CGK-DPS-001"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("FL-1-CGK-DPS-001").passengerId(p1).build(),
                        BookingPassenger.builder().bookingId("FL-1-CGK-DPS-001").passengerId(p2).build()
                ));

        // Available seats for allocation
        List<Seat> seats = List.of(
                seat(1, 10, "EC001", false, null),
                seat(2, 10, "EC002", false, null),
                seat(3, 10, "EC003", true, UUID.randomUUID()) // booked -> ignored
        );
        when(seatRepository.findAll()).thenReturn(seats);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // Update classFlight availability
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDTO res = service.createBooking(dto);

        assertNotNull(res);
        assertEquals("FL-1-CGK-DPS-001", res.getId());
        assertEquals(2, res.getPassengerCount());
        verify(seatRepository, times(2)).save(any(Seat.class));
        verify(classFlightRepository).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("createBooking: success with explicit seatIds")
    void createBooking_success_withSeatIds() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(2)
                .seatIds(List.of(1, 2))
                .passengers(List.of(
                        AddPassengerRequestDTO.builder().idPassport("P1").fullName("A").build(),
                        AddPassengerRequestDTO.builder().idPassport("P2").fullName("B").build()
                ))
                .build();

        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));

        when(bookingRepository.findMaxBookingNumberByPrefix("FL-1-CGK-DPS-")).thenReturn(5);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId("FL-1-CGK-DPS-006");
            return b;
        });

        when(passengerRepository.existsByIdPassport(anyString())).thenReturn(false);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        when(bookingPassengerRepository.findByBookingId("FL-1-CGK-DPS-006"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("FL-1-CGK-DPS-006").passengerId(p1).build(),
                        BookingPassenger.builder().bookingId("FL-1-CGK-DPS-006").passengerId(p2).build()
                ));

        // Explicit seats exist, unbooked, in same class
        when(seatRepository.findById(1)).thenReturn(Optional.of(seat(1, 10, "EC001", false, null)));
        when(seatRepository.findById(2)).thenReturn(Optional.of(seat(2, 10, "EC002", false, null)));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDTO res = service.createBooking(dto);
        assertNotNull(res);
        assertEquals("FL-1-CGK-DPS-006", res.getId());
        verify(seatRepository, times(2)).save(any(Seat.class));
        verify(classFlightRepository).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("createBooking: success with existing passenger")
    void createBooking_success_existingPassenger() {
        AddBookingRequestDTO dto = AddBookingRequestDTO.builder()
                .flightId("FL-1").classFlightId(10).passengerCount(1)
                .contactEmail("e@x.com").contactPhone("08123")
                .passengers(List.of(
                        AddPassengerRequestDTO.builder().idPassport("P1").fullName("Existing A").build()
                ))
                .build();

        // Flight and classFlight valid
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 10, "1000000")));

        // Booking code generation
        when(bookingRepository.findMaxBookingNumberByPrefix("FL-1-CGK-DPS-")).thenReturn(null);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getId() == null) b.setId("FL-1-CGK-DPS-001");
            return b;
        });

        // Passenger exists
        UUID existingP1 = UUID.randomUUID();
        when(passengerRepository.existsByIdPassport("P1")).thenReturn(true);
        when(passengerRepository.findByIdPassport("P1")).thenReturn(Passenger.builder().id(existingP1).fullName("Existing A").idPassport("P1").build());

        // Booking-passengers
        when(bookingPassengerRepository.findByBookingId("FL-1-CGK-DPS-001"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("FL-1-CGK-DPS-001").passengerId(existingP1).build()
                ));

        // Available seats
        List<Seat> seats = List.of(seat(1, 10, "EC001", false, null));
        when(seatRepository.findAll()).thenReturn(seats);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // Update classFlight
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDTO res = service.createBooking(dto);

        assertNotNull(res);
        assertEquals("FL-1-CGK-DPS-001", res.getId());
        assertEquals(1, res.getPassengerCount());
        verify(seatRepository, times(1)).save(any(Seat.class));
        verify(classFlightRepository).save(any(ClassFlight.class));
        // Verify no new passenger saved
        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    // -------------------- simple getters --------------------

    @Test
    @DisplayName("getAllBookings(): default returns non-deleted")
    void getAllBookings_default() {
        when(bookingRepository.findByIsDeleted(false)).thenReturn(List.of(booking("B1", "FL-1", 10, 1, 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId("B1")).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getAllBookings();
        assertEquals(1, res.size());
        assertEquals("B1", res.get(0).getId());
    }

    @Test
    @DisplayName("getAllBookings(includeDeleted=true): returns all")
    void getAllBookings_includeDeletedTrue() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking("B1", "FL-1", 10, 1, 1, false), booking("B2", "FL-2", 11, 1, 1, true)));
        when(classFlightRepository.findById(anyInt())).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId(anyString())).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getAllBookings(true);
        assertEquals(2, res.size());
    }

    @Test
    @DisplayName("getBookingsByFlight(): returns non-deleted for a flight")
    void getBookingsByFlight_default() {
        when(bookingRepository.findByFlightIdAndIsDeleted("FL-1", false)).thenReturn(List.of(booking("B1", "FL-1", 10, 1, 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId("B1")).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getBookingsByFlight("FL-1");
        assertEquals(1, res.size());
        assertEquals("B1", res.get(0).getId());
    }

    @Test
    @DisplayName("getBookingsByFlight(includeDeleted=true): returns all for flight")
    void getBookingsByFlight_includeDeletedTrue() {
        when(bookingRepository.findAll()).thenReturn(List.of(
                booking("B1", "FL-1", 10, 1, 1, false), booking("B2", "FL-1", 10, 1, 1, true), booking("BX", "FL-2", 11, 1, 1, false)
        ));
        when(classFlightRepository.findById(anyInt())).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId(anyString())).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getBookingsByFlight("FL-1", true);
        assertEquals(2, res.size());
    }

    @Test
    @DisplayName("getBooking(): returns null when not found and DTO when found")
    void getBooking_foundAndNotFound() {
        when(bookingRepository.findById("NF")).thenReturn(Optional.empty());
        assertNull(service.getBooking("NF"));

        when(bookingRepository.findById("B1")).thenReturn(Optional.of(booking("B1", "FL-1", 10, 1, 1, false)));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId("B1")).thenReturn(Collections.emptyList());
        assertNotNull(service.getBooking("B1"));
    }

    // -------------------- updateBooking --------------------

    @Test
    @DisplayName("updateBooking: not found or deleted -> null")
    void updateBooking_notFoundOrDeleted() {
        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder().id("U1").build();
        when(bookingRepository.findById("U1")).thenReturn(Optional.empty());
        assertNull(service.updateBooking(dto));

        when(bookingRepository.findById("U2")).thenReturn(Optional.of(booking("U2", "FL-1", 10, 1, 1, true)));
        assertNull(service.updateBooking(UpdateBookingRequestDTO.builder().id("U2").build()));
    }

    @Test
    @DisplayName("updateBooking: invalid booking status -> IllegalStateException")
    void updateBooking_statusInvalid() {
        when(bookingRepository.findById("U3")).thenReturn(Optional.of(booking("U3", "FL-1", 10, 1, 3, false)));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateBooking(UpdateBookingRequestDTO.builder().id("U3").build()));
        assertTrue(ex.getMessage().contains("Only unpaid or paid bookings can be updated"));
    }

    @Test
    @DisplayName("updateBooking: related flight invalid -> IllegalStateException")
    void updateBooking_relatedFlightInvalid() {
        when(bookingRepository.findById("U4")).thenReturn(Optional.of(booking("U4", "FL-1", 10, 1, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 2, false)));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateBooking(UpdateBookingRequestDTO.builder().id("U4").build()));
        assertTrue(ex.getMessage().contains("flight is not scheduled or delayed"));
    }

    @Test
    @DisplayName("updateBooking: seatIds size mismatch -> IllegalArgumentException")
    void updateBooking_seatIdsMismatch() {
        when(bookingRepository.findById("U5")).thenReturn(Optional.of(booking("U5", "FL-1", 10, 2, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        // keep passenger count stable to avoid recompute branch that requires ClassFlight
        when(bookingPassengerRepository.findByBookingId("U5"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("U5").passengerId(UUID.randomUUID()).build(),
                        BookingPassenger.builder().bookingId("U5").passengerId(UUID.randomUUID()).build()
                ));

        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U5").seatIds(List.of(1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking(dto));
        assertTrue(ex.getMessage().contains("Number of seat IDs must match"));
    }

    @Test
    @DisplayName("updateBooking: duplicate seatIds -> IllegalArgumentException")
    void updateBooking_duplicateSeatIds() {
        when(bookingRepository.findById("U6")).thenReturn(Optional.of(booking("U6", "FL-1", 10, 2, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        // keep passenger count stable to avoid recompute branch that requires ClassFlight
        when(bookingPassengerRepository.findByBookingId("U6"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("U6").passengerId(UUID.randomUUID()).build(),
                        BookingPassenger.builder().bookingId("U6").passengerId(UUID.randomUUID()).build()
                ));

        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U6").seatIds(List.of(1, 1))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking(dto));
        assertTrue(ex.getMessage().contains("Duplicate seat IDs"));
    }

    @Test
    @DisplayName("updateBooking: seat belongs to different class -> IllegalArgumentException")
    void updateBooking_seatWrongClass() {
        when(bookingRepository.findById("U7")).thenReturn(Optional.of(booking("U7", "FL-1", 10, 1, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(seatRepository.findById(5)).thenReturn(Optional.of(seat(5, 11, "EC001", false, null)));
        // ensure downstream passenger lookups in validation branch have non-null list
        when(bookingPassengerRepository.findByBookingId("U7"))
                .thenReturn(Collections.singletonList(BookingPassenger.builder().bookingId("U7").passengerId(UUID.randomUUID()).build()));

        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U7").seatIds(List.of(5))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking(dto));
        assertTrue(ex.getMessage().contains("does not belong to the booking's class flight"));
    }

    @Test
    @DisplayName("updateBooking: seat booked by other -> IllegalArgumentException")
    void updateBooking_seatBookedByOther() {
        UUID other = UUID.randomUUID();
        when(bookingRepository.findById("U8")).thenReturn(Optional.of(booking("U8", "FL-1", 10, 1, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // current passengers for booking (different id set)
        when(bookingPassengerRepository.findByBookingId("U8"))
                .thenReturn(List.of(BookingPassenger.builder().bookingId("U8").passengerId(UUID.randomUUID()).build()));

        when(seatRepository.findById(7)).thenReturn(Optional.of(seat(7, 10, "EC007", true, other)));

        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U8").seatIds(List.of(7))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateBooking(dto));
        assertTrue(ex.getMessage().contains("Seat with ID 7 is already booked"));
    }

    @Test
    @DisplayName("updateBooking: success auto-allocates missing seats and updates totals")
    void updateBooking_success_autoAllocate() {
        // Booking with 2 passengers, 0 seats currently mapped -> allocate 2 seats
        Booking existing = booking("U9", "FL-1", 10, 2, 1, false);
        when(bookingRepository.findById("U9")).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));

        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        when(bookingPassengerRepository.findByBookingId("U9"))
                .thenReturn(List.of(
                        BookingPassenger.builder().bookingId("U9").passengerId(p1).build(),
                        BookingPassenger.builder().bookingId("U9").passengerId(p2).build()
                ));

        // No seats mapped currently for those passengers
        when(seatRepository.findAll()).thenReturn(List.of(
                seat(1, 10, "EC001", false, null),
                seat(2, 10, "EC002", false, null)
        ));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // classFlight to compute/ensure current pricing
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U9").contactEmail("new@x.com").contactPhone("08xx")
                .build();

        BookingResponseDTO res = service.updateBooking(dto);

        assertNotNull(res);
        assertEquals("U9", res.getId());
        verify(seatRepository, times(2)).save(any(Seat.class));
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    @DisplayName("updateBooking: success with passenger updates")
    void updateBooking_success_withPassengerUpdates() {
        // Booking with 1 passenger
        Booking existing = booking("U10", "FL-1", 10, 1, 1, false);
        when(bookingRepository.findById("U10")).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));

        UUID p1 = UUID.randomUUID();
        when(bookingPassengerRepository.findByBookingId("U10"))
                .thenReturn(List.of(BookingPassenger.builder().bookingId("U10").passengerId(p1).build()));

        // For passenger update
        when(passengerRepository.findById(p1)).thenReturn(Optional.of(Passenger.builder().id(p1).fullName("Old").build()));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));

        when(seatRepository.findAll()).thenReturn(List.of(seat(1, 10, "EC001", false, null)));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));

        UpdateBookingRequestDTO dto = UpdateBookingRequestDTO.builder()
                .id("U10").contactEmail("new@x.com").contactPhone("08xx")
                .passengers(List.of(apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO.builder()
                        .id(p1).fullName("New Name").birthDate(java.time.LocalDate.now()).gender(1).idPassport("P1").build()))
                .build();

        BookingResponseDTO res = service.updateBooking(dto);

        assertNotNull(res);
        assertEquals("U10", res.getId());
        verify(passengerRepository).save(any(Passenger.class));
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    // -------------------- deleteBooking --------------------

    @Test
    @DisplayName("deleteBooking: not found -> null")
    void deleteBooking_notFound() {
        when(bookingRepository.findById("D1")).thenReturn(Optional.empty());
        assertNull(service.deleteBooking("D1"));
    }

    @Test
    @DisplayName("deleteBooking: already deleted -> IllegalStateException")
    void deleteBooking_alreadyDeleted() {
        when(bookingRepository.findById("D2")).thenReturn(Optional.of(booking("D2", "FL-1", 10, 1, 1, true)));
        assertThrows(IllegalStateException.class, () -> service.deleteBooking("D2"));
    }

    @Test
    @DisplayName("deleteBooking: invalid status -> IllegalStateException")
    void deleteBooking_invalidStatus() {
        when(bookingRepository.findById("D3")).thenReturn(Optional.of(booking("D3", "FL-1", 10, 1, 3, false)));
        assertThrows(IllegalStateException.class, () -> service.deleteBooking("D3"));
    }

    @Test
    @DisplayName("deleteBooking: related flight invalid -> IllegalStateException")
    void deleteBooking_relatedFlightInvalid() {
        when(bookingRepository.findById("D4")).thenReturn(Optional.of(booking("D4", "FL-1", 10, 1, 1, false)));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 2, false)));
        assertThrows(IllegalStateException.class, () -> service.deleteBooking("D4"));
    }

    @Test
    @DisplayName("deleteBooking: success deallocates seats, restores availability and cancels booking")
    void deleteBooking_success() {
        Booking b = booking("D5", "FL-1", 10, 2, 1, false);
        when(bookingRepository.findById("D5")).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // Flight valid status
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight("FL-1", "CGK", "DPS", 1, false)));

        // booking passengers
        UUID p1 = UUID.randomUUID(); UUID p2 = UUID.randomUUID();
        when(bookingPassengerRepository.findByBookingId("D5")).thenReturn(List.of(
                BookingPassenger.builder().bookingId("D5").passengerId(p1).build(),
                BookingPassenger.builder().bookingId("D5").passengerId(p2).build()
        ));

        // seats allocated to those passengers -> deallocate
        when(seatRepository.findAll()).thenReturn(List.of(
                seat(1, 10, "EC001", true, p1),
                seat(2, 10, "EC002", true, p2),
                seat(3, 10, "EC003", false, null)
        ));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // restore class availability
        ClassFlight cf = classFlight(10, "economy", 10, 5, "1000000");
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(cf));
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDTO res = service.deleteBooking("D5");
        assertNotNull(res);
        assertEquals(7, cf.getAvailableSeats());
        assertEquals(3, res.getStatus()); // Cancelled
        verify(seatRepository, times(2)).save(any(Seat.class));
        verify(classFlightRepository).save(cf);
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    @DisplayName("getAllBookings(includeDeleted=true): returns all bookings")
    void getAllBookings_includeDeleted() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking("B1", "FL-1", 10, 1, 1, false), booking("B2", "FL-2", 11, 1, 1, true)));
        when(classFlightRepository.findById(anyInt())).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId(anyString())).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getAllBookings(true);
        assertEquals(2, res.size());
    }

    @Test
    @DisplayName("getBookingsByFlight with filters: applies search, contactEmail, status")
    void getBookingsByFlight_withFilters() {
        var b1 = booking("B1", "FL-1", 10, 1, 1, false);
        b1.setContactEmail("test@x.com");
        var b2 = booking("B2", "FL-1", 10, 1, 2, false);
        b2.setContactEmail("other@y.com");
        when(bookingRepository.findByFlightIdAndIsDeleted("FL-1", false)).thenReturn(List.of(b1, b2));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(classFlight(10, "economy", 10, 8, "1000000")));
        when(bookingPassengerRepository.findByBookingId(anyString())).thenReturn(Collections.emptyList());

        List<BookingResponseDTO> res = service.getBookingsByFlight("FL-1", false, "B1", "test@x.com", 1);
        assertEquals(1, res.size());
        assertEquals("B1", res.get(0).getId());
    }

    @Test
    @DisplayName("getTodayBookings: counts bookings created today")
    void getTodayBookings() {
        var today = LocalDateTime.now();
        var b1 = booking("B1", "FL-1", 10, 1, 1, false);
        b1.setCreatedAt(today);
        var b2 = booking("B2", "FL-1", 10, 1, 1, false);
        b2.setCreatedAt(today.minusDays(1)); // not today
        when(bookingRepository.findByIsDeleted(false)).thenReturn(List.of(b1, b2));

        long count = service.getTodayBookings();
        assertEquals(1, count);
    }


    @Test
    @DisplayName("getBookingChart: computes stats for month/year")
    void getBookingChart() {
        var now = LocalDateTime.now();
        var b1 = booking("B1", "FL-1", 10, 1, 1, false); // Unpaid
        b1.setCreatedAt(now.withMonth(10).withYear(2024));
        var b2 = booking("B2", "FL-2", 10, 1, 2, false); // Paid
        b2.setCreatedAt(now.withMonth(10).withYear(2024));
        b2.setTotalPrice(BigDecimal.valueOf(100));
        lenient().when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));
        lenient().when(airlineRepository.findById(anyString())).thenReturn(Optional.of(apap.ti._5.flight_2306211660_be.model.Airline.builder().name("Airline1").build()));

        var chart = service.getBookingChart(10, 2024);
        assertEquals(2, chart.size());
        // Since not filtered by month/year, and grouped by flightId
        var fl1 = chart.stream().filter(c -> "FL-1".equals(c.getFlightId())).findFirst().orElse(null);
        var fl2 = chart.stream().filter(c -> "FL-2".equals(c.getFlightId())).findFirst().orElse(null);
        assertNotNull(fl1);
        assertNotNull(fl2);
        assertEquals(1L, fl1.getTotalBookings());
        assertEquals(BigDecimal.ZERO, fl1.getTotalRevenue());
        assertEquals(1L, fl2.getTotalBookings());
        assertEquals(BigDecimal.valueOf(100), fl2.getTotalRevenue());
    }

    @Test
    @DisplayName("getBookingChartData: returns chart and summary")
    void getBookingChartData() {
        var now = LocalDateTime.now();
        var b1 = booking("B1", "FL-1", 10, 1, 2, false); // Paid
        b1.setCreatedAt(now.withMonth(10).withYear(2024));
        b1.setTotalPrice(BigDecimal.valueOf(100));
        lenient().when(bookingRepository.findAll()).thenReturn(List.of(b1));
        lenient().when(airlineRepository.findById(anyString())).thenReturn(Optional.of(apap.ti._5.flight_2306211660_be.model.Airline.builder().name("Airline1").build()));

        var data = service.getBookingChartData(10, 2024);
        assertNotNull(data.getChart());
        assertNotNull(data.getSummary());
        assertEquals(1L, data.getSummary().getTotalBookings());
        assertEquals(BigDecimal.valueOf(100), data.getSummary().getTotalRevenue());
    }
}

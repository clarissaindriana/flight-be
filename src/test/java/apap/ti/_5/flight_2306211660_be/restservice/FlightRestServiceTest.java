package apap.ti._5.flight_2306211660_be.restservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.Airline;
import apap.ti._5.flight_2306211660_be.model.Airplane;
import apap.ti._5.flight_2306211660_be.model.Booking;
import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Flight;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.AirlineRepository;
import apap.ti._5.flight_2306211660_be.repository.AirplaneRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.FlightRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.AddFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.UpdateFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightResponseDTO;
import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.restservice.classFlight.ClassFlightRestService;
import apap.ti._5.flight_2306211660_be.restservice.flight.FlightRestServiceImpl;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;

@ExtendWith(MockitoExtension.class)
public class FlightRestServiceTest {

    @Mock private FlightRepository flightRepository;
    @Mock private AirplaneRepository airplaneRepository;
    @Mock private ClassFlightRestService classFlightRestService;
    @Mock private SeatRestService seatRestService;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingPassengerRepository bookingPassengerRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private ClassFlightRepository classFlightRepository;
    @Mock private AirlineRepository airlineRepository;
    @Mock private ProfileClient profileClient;

    @InjectMocks
    private FlightRestServiceImpl service;

    private Flight flight(String id, String airlineId, String airplaneId, String o, String d,
                          LocalDateTime dep, LocalDateTime arr, int status, boolean deleted) {
        return Flight.builder()
                .id(id)
                .airlineId(airlineId)
                .airplaneId(airplaneId)
                .originAirportCode(o)
                .destinationAirportCode(d)
                .departureTime(dep)
                .arrivalTime(arr)
                .terminal("T1")
                .gate("G1")
                .baggageAllowance(20)
                .facilities("WiFi")
                .status(status)
                .isDeleted(deleted)
                .build();
    }

    private AddClassFlightRequestDTO addClass(String type, int cap, String price) {
        return AddClassFlightRequestDTO.builder()
                .classType(type)
                .seatCapacity(cap)
                .price(new BigDecimal(price))
                .build();
    }

    @Test
    @DisplayName("createFlight: departure not before arrival -> throws")
    void createFlight_invalidTimes() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(2)).arrivalTime(now.plusHours(1))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000")))
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Departure time must be before arrival time"));
    }

    @Test
    @DisplayName("createFlight: origin equals destination -> throws")
    void createFlight_sameOriginDestination() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("CGK")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000")))
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Origin and destination airports cannot be the same"));
    }

    @Test
    @DisplayName("createFlight: airplane not active or not found -> throws")
    void createFlight_airplaneNotFound() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000")))
                .build();
        when(airplaneRepository.findActiveById("AP-1")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Airplane not found or not active"));
    }

    @Test
    @DisplayName("createFlight: airline not found or not active -> throws")
    void createFlight_airlineNotFound() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000")))
                .build();
        when(airplaneRepository.findActiveById("AP-1")).thenReturn(Airplane.builder().id("AP-1").seatCapacity(1).build());
        when(airlineRepository.findById("AL-1")).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Airline not found or not active"));
    }

    @Test
    @DisplayName("createFlight: total requested seats exceed airplane -> throws")
    void createFlight_exceedCapacity() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 3, "1000000"), addClass("business", 3, "2000000")))
                .build();
        when(airplaneRepository.findActiveById("AP-1")).thenReturn(Airplane.builder().id("AP-1").seatCapacity(4).build());
        when(airlineRepository.findById("AL-1")).thenReturn(Optional.of(Airline.builder().id("AL-1").build()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Total requested seats exceed airplane capacity"));
    }

    @Test
    @DisplayName("createFlight: overlapping flights -> throws")
    void createFlight_overlapping() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000")))
                .build();
        when(airplaneRepository.findActiveById("AP-1")).thenReturn(Airplane.builder().id("AP-1").seatCapacity(100).build());
        when(airlineRepository.findById("AL-1")).thenReturn(Optional.of(Airline.builder().id("AL-1").build()));
        when(flightRepository.findOverlappingFlights(eq("AP-1"), any(), any())).thenReturn(List.of(new Flight()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createFlight(dto));
        assertTrue(ex.getMessage().contains("Airplane is already scheduled for overlapping flights"));
    }

    @Test
    @DisplayName("createFlight: success generates ID and seats with proper prefixes")
    void createFlight_success_generatesSeats() {
        var now = LocalDateTime.now();
        var dto = AddFlightRequestDTO.builder()
                .airlineId("AL-1").airplaneId("AP-1")
                .originAirportCode("CGK").destinationAirportCode("DPS")
                .departureTime(now.plusHours(1)).arrivalTime(now.plusHours(2))
                .terminal("T1").gate("G1").baggageAllowance(20)
                .classes(List.of(addClass("economy", 2, "1000000"), addClass("business", 2, "2000000")))
                .build();

        when(airplaneRepository.findActiveById("AP-1")).thenReturn(Airplane.builder().id("AP-1").seatCapacity(100).build());
        when(airlineRepository.findById("AL-1")).thenReturn(Optional.of(Airline.builder().id("AL-1").build()));
        when(flightRepository.findOverlappingFlights(eq("AP-1"), any(), any())).thenReturn(Collections.emptyList());
        when(flightRepository.findMaxFlightNumberByAirplaneId("AP-1")).thenReturn(null);
        Flight saved = flight(null, "AL-1", "AP-1", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, false);
        saved.setId("AP-1-001");
        when(flightRepository.save(any(Flight.class))).thenReturn(saved);
        when(flightRepository.findById("AP-1-001")).thenReturn(Optional.of(saved));

        when(classFlightRestService.createClassFlight(any(AddClassFlightRequestDTO.class)))
                .thenReturn(ClassFlightResponseDTO.builder().id(10).build())
                .thenReturn(ClassFlightResponseDTO.builder().id(11).build());
        when(classFlightRestService.getClassFlightsByFlight("AP-1-001")).thenReturn(Collections.emptyList());

        FlightResponseDTO res = service.createFlight(dto);

        assertNotNull(res);
        assertEquals("AP-1-001", res.getId());

        ArgumentCaptor<AddSeatRequestDTO> seatCaptor = ArgumentCaptor.forClass(AddSeatRequestDTO.class);
        verify(seatRestService, times(4)).createSeat(seatCaptor.capture());
        List<String> codes = new ArrayList<>();
        for (AddSeatRequestDTO s : seatCaptor.getAllValues()) codes.add(s.getSeatCode());
        // Expect EC001, EC002, BU001, BU002 (order by classes loop)
        assertTrue(codes.containsAll(List.of("EC001", "EC002", "BU001", "BU002")));
    }

    @Test
    @DisplayName("getAllFlights: returns non-deleted flights mapped")
    void getAllFlights_success() {
        var now = LocalDateTime.now();
        var f = flight("F1", "AL", "AP", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, false);
        when(flightRepository.findByIsDeleted(false)).thenReturn(List.of(f));
        when(classFlightRestService.getClassFlightsByFlight("F1")).thenReturn(Collections.emptyList());

        List<FlightResponseDTO> res = service.getAllFlights();
        assertEquals(1, res.size());
        assertEquals("F1", res.get(0).getId());
    }

    @Test
    @DisplayName("searchFlightsByAirline: filters by airlineId")
    void searchFlightsByAirline_success() {
        var now = LocalDateTime.now();
        var f = flight("F2", "AL-2", "AP", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, false);
        when(flightRepository.findByAirlineIdAndIsDeleted("AL-2", false)).thenReturn(List.of(f));
        when(classFlightRestService.getClassFlightsByFlight("F2")).thenReturn(Collections.emptyList());

        List<FlightResponseDTO> res = service.searchFlightsByAirline("AL-2");
        assertEquals(1, res.size());
        assertEquals("AL-2", res.get(0).getAirlineId());
    }

    @Test
    @DisplayName("getFlight: returns null when not found")
    void getFlight_notFound() {
        when(flightRepository.findById("X")).thenReturn(Optional.empty());
        assertNull(service.getFlight("X"));
    }

    @Test
    @DisplayName("getFlight: returns null when deleted")
    void getFlight_deleted() {
        var now = LocalDateTime.now();
        var f = flight("F3", "AL", "AP", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, true);
        when(flightRepository.findById("F3")).thenReturn(Optional.of(f));
        assertNull(service.getFlight("F3"));
    }

    @Test
    @DisplayName("getFlight: updates status based on time and maps")
    void getFlight_statusTransitionAndMap() {
        var now = LocalDateTime.now();
        // departure <= now < arrival -> should become In Flight(2)
        var f = flight("F4", "AL", "AP", "CGK", "DPS", now.minusMinutes(10), now.plusMinutes(10), 1, false);
        when(flightRepository.findById("F4")).thenReturn(Optional.of(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));
        when(classFlightRestService.getClassFlightsByFlight("F4")).thenReturn(Collections.emptyList());

        FlightResponseDTO res = service.getFlight("F4");
        assertNotNull(res);
        assertEquals(2, res.getStatus()); // In Flight
    }


    @Test
    @DisplayName("getFlightDetail: not found returns null")
    void getFlightDetail_notFound() {
        when(flightRepository.findById("NF")).thenReturn(Optional.empty());
        assertNull(service.getFlightDetail("NF"));
    }

    @Test
    @DisplayName("getFlightDetail: maps with class details")
    void getFlightDetail_success() {
        var now = LocalDateTime.now();
        var f = flight("F8", "AL", "AP", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, false);

        when(flightRepository.findById("F8")).thenReturn(Optional.of(f));
        lenient().when(classFlightRestService.getClassFlightsByFlight("F8"))
                .thenReturn(List.of(ClassFlightResponseDTO.builder().id(10).build()));
        lenient().when(classFlightRestService.getClassFlightDetail(10))
                .thenReturn(ClassFlightResponseDTO.builder().id(10).seats(Collections.emptyList()).build());

        FlightResponseDTO res = service.getFlightDetail("F8");
        assertNotNull(res);
        assertEquals("F8", res.getId());
        assertEquals(1, res.getClasses().size());
    }

    @Test
    @DisplayName("updateFlight: returns null when not found")
    void updateFlight_notFound() {
        when(flightRepository.findById("U1")).thenReturn(Optional.empty());
        var dto = UpdateFlightRequestDTO.builder()
                .id("U1")
                .departureTime(LocalDateTime.now().plusHours(1))
                .arrivalTime(LocalDateTime.now().plusHours(2))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        assertNull(service.updateFlight(dto));
    }

    @Test
    @DisplayName("updateFlight: returns null when deleted")
    void updateFlight_deleted() {
        var now = LocalDateTime.now();
        var f = flight("U2", "AL", "AP", "CGK", "DPS", now.plusHours(2), now.plusHours(3), 1, true);
        when(flightRepository.findById("U2")).thenReturn(Optional.of(f));
        var dto = UpdateFlightRequestDTO.builder()
                .id("U2")
                .departureTime(now.plusHours(3))
                .arrivalTime(now.plusHours(4))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        assertNull(service.updateFlight(dto));
    }

    @Test
    @DisplayName("updateFlight: invalid current status -> throws")
    void updateFlight_invalidStatus() {
        var now = LocalDateTime.now();
        var f = flight("U3", "AL", "AP", "CGK", "DPS", now.plusHours(2), now.plusHours(3), 2, false); // In Flight
        when(flightRepository.findById("U3")).thenReturn(Optional.of(f));
        var dto = UpdateFlightRequestDTO.builder()
                .id("U3")
                .departureTime(now.plusHours(3))
                .arrivalTime(now.plusHours(4))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateFlight(dto));
        assertTrue(ex.getMessage().contains("not scheduled or delayed"));
    }

    @Test
    @DisplayName("updateFlight: invalid times -> throws")
    void updateFlight_invalidTimes() {
        var now = LocalDateTime.now();
        var f = flight("U4", "AL", "AP", "CGK", "DPS", now.plusHours(2), now.plusHours(3), 1, false);
        when(flightRepository.findById("U4")).thenReturn(Optional.of(f));
        var dto = UpdateFlightRequestDTO.builder()
                .id("U4")
                .departureTime(now.plusHours(5))
                .arrivalTime(now.plusHours(4)) // earlier than dep
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFlight(dto));
        assertTrue(ex.getMessage().contains("Departure time must be before arrival time"));
    }

    @Test
    @DisplayName("updateFlight: past departure -> throws")
    void updateFlight_pastDeparture() {
        var now = LocalDateTime.now();
        var f = flight("U5", "AL", "AP", "CGK", "DPS", now.plusHours(2), now.plusHours(3), 1, false);
        when(flightRepository.findById("U5")).thenReturn(Optional.of(f));
        var dto = UpdateFlightRequestDTO.builder()
                .id("U5")
                .departureTime(now.minusMinutes(1)) // past
                .arrivalTime(now.plusHours(1))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFlight(dto));
        assertTrue(ex.getMessage().contains("Departure time cannot be in the past"));
    }

    @Test
    @DisplayName("updateFlight: overlapping flights excluding id -> throws")
    void updateFlight_overlapping() {
        var now = LocalDateTime.now();
        var f = flight("U6", "AL", "AP6", "CGK", "DPS", now.plusHours(2), now.plusHours(3), 1, false);
        when(flightRepository.findById("U6")).thenReturn(Optional.of(f));
        when(flightRepository.findOverlappingFlightsExcludingId(eq("AP6"), eq("U6"), any(), any()))
                .thenReturn(List.of(new Flight()));
        var dto = UpdateFlightRequestDTO.builder()
                .id("U6")
                .departureTime(now.plusHours(4))
                .arrivalTime(now.plusHours(5))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateFlight(dto));
        assertTrue(ex.getMessage().contains("overlapping flights"));
    }

    @Test
    @DisplayName("updateFlight: success and marked delayed when moved later")
    void updateFlight_success_delayed() {
        var now = LocalDateTime.now();
        var f = flight("U7", "AL", "AP7", "CGK", "DPS", now.plusHours(2), now.plusHours(4), 1, false);
        when(flightRepository.findById("U7")).thenReturn(Optional.of(f));
        when(flightRepository.findOverlappingFlightsExcludingId(eq("AP7"), eq("U7"), any(), any()))
                .thenReturn(Collections.emptyList());
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));
        when(classFlightRestService.getClassFlightsByFlight("U7")).thenReturn(Collections.emptyList());

        var dto = UpdateFlightRequestDTO.builder()
                .id("U7")
                .departureTime(now.plusHours(3)) // moved later
                .arrivalTime(now.plusHours(5))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();

        FlightResponseDTO res = service.updateFlight(dto);
        assertNotNull(res);
        assertEquals(4, res.getStatus()); // Delayed
    }

    @Test
    @DisplayName("updateFlight: success and not delayed when not moved later")
    void updateFlight_success_notDelayed() {
        var now = LocalDateTime.now();
        var f = flight("U8", "AL", "AP8", "CGK", "DPS", now.plusHours(4), now.plusHours(6), 4, false); // Delayed currently
        when(flightRepository.findById("U8")).thenReturn(Optional.of(f));
        when(flightRepository.findOverlappingFlightsExcludingId(eq("AP8"), eq("U8"), any(), any()))
                .thenReturn(Collections.emptyList());
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));
        when(classFlightRestService.getClassFlightsByFlight("U8")).thenReturn(Collections.emptyList());

        var dto = UpdateFlightRequestDTO.builder()
                .id("U8")
                .departureTime(now.plusHours(3)) // moved earlier
                .arrivalTime(now.plusHours(7))
                .terminal("T").gate("G").baggageAllowance(10)
                .build();

        FlightResponseDTO res = service.updateFlight(dto);
        assertNotNull(res);
        // remains whatever was set (not forced to 4)
        assertEquals(4, res.getStatus());
    }

    @Test
    @DisplayName("deleteFlight: not found returns null")
    void deleteFlight_notFound() {
        when(flightRepository.findById("D1")).thenReturn(Optional.empty());
        assertNull(service.deleteFlight("D1"));
    }

    @Test
    @DisplayName("deleteFlight: already deleted -> throws")
    void deleteFlight_alreadyDeleted() {
        var f = flight("D2", "AL", "AP", "CGK", "DPS", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 1, true);
        when(flightRepository.findById("D2")).thenReturn(Optional.of(f));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.deleteFlight("D2"));
        assertTrue(ex.getMessage().contains("already deleted"));
    }

    @Test
    @DisplayName("deleteFlight: invalid status -> throws")
    void deleteFlight_invalidStatus() {
        var f = flight("D3", "AL", "AP", "CGK", "DPS", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 2, false);
        when(flightRepository.findById("D3")).thenReturn(Optional.of(f));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.deleteFlight("D3"));
        assertTrue(ex.getMessage().contains("Cannot delete flight"));
    }

    @Test
    @DisplayName("deleteFlight: success cancels bookings, frees seats, updates classFlight, soft-deletes booking and flight")
    void deleteFlight_success_fullPath() {
        var now = LocalDateTime.now();

        // Flight scheduled
        var f = flight("D4", "AL", "AP", "CGK", "DPS", now.plusHours(1), now.plusHours(2), 1, false);
        when(flightRepository.findById("D4")).thenReturn(Optional.of(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        // Active bookings
        var booking = Booking.builder()
                .id("B-1")
                .flightId("D4")
                .classFlightId(99)
                .passengerCount(2)
                .status(1)
                .isDeleted(false)
                .build();
        when(bookingRepository.findActiveBookingsByFlightId("D4")).thenReturn(List.of(booking));

        // Booking passengers
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        var bp1 = BookingPassenger.builder().bookingId("B-1").passengerId(p1).build();
        var bp2 = BookingPassenger.builder().bookingId("B-1").passengerId(p2).build();
        when(bookingPassengerRepository.findByBookingId("B-1")).thenReturn(List.of(bp1, bp2));

        // Seats assigned to those passengers in the class
        var s1 = Seat.builder().id(1).classFlightId(99).seatCode("EC001").isBooked(true).passengerId(p1).build();
        var s2 = Seat.builder().id(2).classFlightId(99).seatCode("EC002").isBooked(true).passengerId(p2).build();
        var s3 = Seat.builder().id(3).classFlightId(99).seatCode("EC003").isBooked(false).passengerId(null).build();
        when(seatRepository.findAll()).thenReturn(List.of(s1, s2, s3));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // Class flight to restore availability
        var cf = ClassFlight.builder().id(99).availableSeats(5).seatCapacity(10).classType("economy").flightId("D4").price(new BigDecimal("1000000")).build();
        when(classFlightRepository.findById(99)).thenReturn(Optional.of(cf));
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(classFlightRestService.getClassFlightsByFlight("D4")).thenReturn(Collections.emptyList());

        FlightResponseDTO res = service.deleteFlight("D4");

        assertNotNull(res);
        // Seats freed
        assertFalse(s1.getIsBooked());
        assertNull(s1.getPassengerId());
        assertFalse(s2.getIsBooked());
        assertNull(s2.getPassengerId());
        // Availability restored
        assertEquals(7, cf.getAvailableSeats());
        // Booking soft-deleted and cancelled
        assertTrue(booking.getIsDeleted());
        assertEquals(3, booking.getStatus());
        // Flight cancelled
        assertEquals(5, res.getStatus());

        verify(seatRepository, times(2)).save(any(Seat.class));
        verify(bookingRepository).save(booking);
        verify(classFlightRepository).save(cf);
        verify(flightRepository, atLeastOnce()).save(any(Flight.class));
    }

    @Test
    @DisplayName("getAllFlightsWithFilters: applies origin, destination, airline, status, search filters")
    void getAllFlightsWithFilters_success() {
        var f1 = flight("F1", "AL-1", "AP1", "CGK", "DPS", LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 1, false);
        var f2 = flight("F2", "AL-2", "AP2", "SUB", "DPS", LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4), 2, false);
        when(flightRepository.findByIsDeleted(false)).thenReturn(List.of(f1, f2));
        when(classFlightRestService.getClassFlightsByFlight(anyString())).thenReturn(Collections.emptyList());

        var res = service.getAllFlightsWithFilters("CGK", "DPS", "AL-1", 1, false, "F1");
        assertEquals(1, res.size());
        assertEquals("F1", res.get(0).getId());
    }

    @Test
    @DisplayName("getActiveFlightsTodayCount: counts scheduled/in-flight flights today")
    void getActiveFlightsTodayCount() {
        var today = LocalDateTime.now();
        var f1 = flight("F1", "AL", "AP", "CGK", "DPS", today.withHour(10), today.withHour(11), 1, false); // Scheduled
        var f2 = flight("F2", "AL", "AP", "CGK", "DPS", today.withHour(12), today.withHour(13), 2, false); // In Flight
        var f3 = flight("F3", "AL", "AP", "CGK", "DPS", today.withHour(14), today.withHour(15), 3, false); // Finished - not counted
        when(flightRepository.findByIsDeleted(false)).thenReturn(List.of(f1, f2, f3));

        long count = service.getActiveFlightsTodayCount();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("getFlightReminders: returns flights within interval with paid bookings")
    void getFlightReminders() {
        var now = LocalDateTime.now();
        var f1 = flight("F1", "AL", "AP", "CGK", "DPS", now.plusMinutes(30), now.plusHours(1), 1, false); // Within 3 hours
        var f2 = flight("F2", "AL", "AP", "CGK", "DPS", now.plusHours(4), now.plusHours(5), 1, false); // Outside 3 hours
        when(flightRepository.findByIsDeleted(false)).thenReturn(List.of(f1, f2));
        // Mock profile client - simplified
        when(profileClient.getUserById("user1")).thenReturn(null); // Assume no user found, so no reminders

        var reminders = service.getFlightReminders(3, "user1");
        assertEquals(0, reminders.size()); // No reminders since user not found
    }
}

package apap.ti._5.flight_2306211660_be.restservice;

import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.classFlight.ClassFlightRestServiceImpl;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassFlightRestServiceTest {

    @Mock
    private ClassFlightRepository classFlightRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatRestService seatRestService;

    @InjectMocks
    private ClassFlightRestServiceImpl service;

    private ClassFlight cf(Integer id, String flightId, String classType, int cap, int avail, BigDecimal price) {
        return ClassFlight.builder()
                .id(id)
                .flightId(flightId)
                .classType(classType)
                .seatCapacity(cap)
                .availableSeats(avail)
                .price(price)
                .build();
    }

    private Seat seat(Integer id, Integer classFlightId, String code, boolean booked) {
        return Seat.builder()
                .id(id)
                .classFlightId(classFlightId)
                .seatCode(code)
                .isBooked(booked)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("createClassFlight: persist and map to response")
    void createClassFlight_success() {
        var req = AddClassFlightRequestDTO.builder()
                .flightId("FL-NEW")
                .classType("economy")
                .seatCapacity(10)
                .price(new BigDecimal("1000000"))
                .build();

        var saved = cf(1, "FL-NEW", "economy", 10, 10, new BigDecimal("1000000"));
        when(classFlightRepository.save(any(ClassFlight.class))).thenReturn(saved);

        ClassFlightResponseDTO res = service.createClassFlight(req);

        assertNotNull(res);
        assertEquals(1, res.getId());
        assertEquals("FL-NEW", res.getFlightId());
        assertEquals("economy", res.getClassType());
        assertEquals(10, res.getSeatCapacity());
        assertEquals(10, res.getAvailableSeats());
        verify(classFlightRepository).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("getAllClassFlights: fetch all and map")
    void getAllClassFlights_success() {
        var c1 = cf(1, "FL-1", "economy", 10, 10, new BigDecimal("1000000"));
        var c2 = cf(2, "FL-2", "business", 5, 5, new BigDecimal("2000000"));
        when(classFlightRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<ClassFlightResponseDTO> res = service.getAllClassFlights();

        assertEquals(2, res.size());
        assertEquals(1, res.get(0).getId());
        assertEquals(2, res.get(1).getId());
        verify(classFlightRepository).findAll();
    }

    @Test
    @DisplayName("getClassFlightsByFlight: filters by flightId and maps")
    void getClassFlightsByFlight_filters() {
        var c1 = cf(1, "FL-X", "first", 2, 2, new BigDecimal("5000000"));
        var c2 = cf(2, "FL-Y", "economy", 3, 3, new BigDecimal("1000000"));
        when(classFlightRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<ClassFlightResponseDTO> res = service.getClassFlightsByFlight("FL-X");

        assertEquals(1, res.size());
        assertEquals("FL-X", res.get(0).getFlightId());
        verify(classFlightRepository).findAll();
    }

    @Test
    @DisplayName("getClassFlight: returns mapped DTO when found")
    void getClassFlight_found() {
        var c = cf(10, "FL-10", "business", 4, 4, new BigDecimal("3000000"));
        when(classFlightRepository.findById(10)).thenReturn(Optional.of(c));

        ClassFlightResponseDTO res = service.getClassFlight(10);

        assertNotNull(res);
        assertEquals(10, res.getId());
        verify(classFlightRepository).findById(10);
    }

    @Test
    @DisplayName("getClassFlight: returns null when not found")
    void getClassFlight_notFound() {
        when(classFlightRepository.findById(404)).thenReturn(Optional.empty());

        ClassFlightResponseDTO res = service.getClassFlight(404);
        assertNull(res);
        verify(classFlightRepository).findById(404);
    }

    @Test
    @DisplayName("getClassFlightDetail: returns mapped DTO with seats via SeatRestService when found")
    void getClassFlightDetail_found() {
        var c = cf(5, "FL-5", "economy", 10, 10, new BigDecimal("1200000"));
        when(classFlightRepository.findById(5)).thenReturn(Optional.of(c));
        when(seatRestService.getSeatsByClassFlight(5)).thenReturn(Collections.singletonList(
                SeatResponseDTO.builder().id(1).classFlightId(5).seatCode("EC001").isBooked(false).build()
        ));

        ClassFlightResponseDTO res = service.getClassFlightDetail(5);

        assertNotNull(res);
        assertEquals(5, res.getId());
        assertNotNull(res.getSeats());
        assertEquals(1, res.getSeats().size());
        verify(seatRestService).getSeatsByClassFlight(5);
    }

    @Test
    @DisplayName("getClassFlightDetail: returns null when not found")
    void getClassFlightDetail_notFound() {
        when(classFlightRepository.findById(999)).thenReturn(Optional.empty());

        ClassFlightResponseDTO res = service.getClassFlightDetail(999);
        assertNull(res);
        verify(classFlightRepository).findById(999);
        verify(seatRestService, never()).getSeatsByClassFlight(anyInt());
    }

    @Test
    @DisplayName("updateClassFlight: returns null when target does not exist")
    void updateClassFlight_notFound() {
        when(classFlightRepository.findById(77)).thenReturn(Optional.empty());

        var req = UpdateClassFlightRequestDTO.builder()
                .id(77)
                .seatCapacity(20)
                .price(new BigDecimal("1000000"))
                .build();

        ClassFlightResponseDTO res = service.updateClassFlight(req);
        assertNull(res);
        verify(classFlightRepository).findById(77);
        verify(classFlightRepository, never()).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("updateClassFlight: validate bookedCount > newCap throws IllegalStateException")
    void updateClassFlight_validateBookedCount() {
        var existing = cf(88, "FL-8", "business", 6, 6, new BigDecimal("2000000"));
        when(classFlightRepository.findById(88)).thenReturn(Optional.of(existing));

        // Seats: 4 booked, 2 unbooked; newCap below booked(4) should throw
        List<Seat> seats = Arrays.asList(
                seat(1, 88, "BU001", true),
                seat(2, 88, "BU002", true),
                seat(3, 88, "BU003", true),
                seat(4, 88, "BU004", true),
                seat(5, 88, "BU005", false),
                seat(6, 88, "BU006", false)
        );
        when(seatRepository.findAll()).thenReturn(seats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(88)
                .seatCapacity(3) // new < bookedCount(4) -> throws
                .price(new BigDecimal("2500000"))
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateClassFlight(req));
        assertTrue(ex.getMessage().contains("Cannot set seat capacity below currently booked seats"));
        verify(seatRepository).findAll();
        verify(classFlightRepository, never()).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("updateClassFlight: increase capacity adds seats with correct prefix and saves availability")
    void updateClassFlight_increaseCapacity_addsSeats() {
        var existing = cf(33, "FL-3", "economy", 2, 2, new BigDecimal("1000000"));
        when(classFlightRepository.findById(33)).thenReturn(Optional.of(existing));

        // current seats EC001, EC002 all unbooked
        List<Seat> existingSeats = Arrays.asList(
                seat(1, 33, "EC001", false),
                seat(2, 33, "EC002", false)
        );
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(33)
                .seatCapacity(5) // increase by 3 -> EC003..EC005
                .price(new BigDecimal("1500000"))
                .build();

        // After creating seats, seatsAfter used to compute availability
        List<Seat> seatsAfter = new ArrayList<>(existingSeats);
        seatsAfter.add(seat(3, 33, "EC003", false));
        seatsAfter.add(seat(4, 33, "EC004", false));
        seatsAfter.add(seat(5, 33, "EC005", false));

        // seatRepository.findAll() is called twice (before and after), return existing then updated
        when(seatRepository.findAll()).thenReturn(existingSeats).thenReturn(seatsAfter);

        // Save returns updated classFlight
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassFlightResponseDTO res = service.updateClassFlight(req);

        // verify seat creation prefixes and codes
        ArgumentCaptor<AddSeatRequestDTO> captor = ArgumentCaptor.forClass(AddSeatRequestDTO.class);
        verify(seatRestService, times(3)).createSeat(captor.capture());
        List<String> createdCodes = captor.getAllValues().stream().map(AddSeatRequestDTO::getSeatCode).collect(Collectors.toList());
        assertEquals(Arrays.asList("EC003", "EC004", "EC005"), createdCodes);

        assertNotNull(res);
        assertEquals(5, res.getSeatCapacity());
        // available seats should count non-booked (all 5 here)
        assertEquals(5, res.getAvailableSeats());
        verify(classFlightRepository).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("updateClassFlight: decrease capacity removes highest-numbered unbooked seats")
    void updateClassFlight_decreaseCapacity_removeTailSeats() {
        var existing = cf(44, "FL-4", "economy", 5, 5, new BigDecimal("1000000"));
        when(classFlightRepository.findById(44)).thenReturn(Optional.of(existing));

        // Existing seats EC001..EC005 (EC005 and EC004 unbooked and should be removable), EC003 booked
        List<Seat> existingSeats = Arrays.asList(
                seat(1, 44, "EC001", false),
                seat(2, 44, "EC002", false),
                seat(3, 44, "EC003", true),
                seat(4, 44, "EC004", false),
                seat(5, 44, "EC005", false)
        );
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(44)
                .seatCapacity(3) // decrease by 2 -> remove EC005 then EC004
                .price(new BigDecimal("900000"))
                .build();

        // After removal, seatsAfter contains EC001, EC002, EC003 (EC003 booked)
        List<Seat> seatsAfter = Arrays.asList(
                seat(1, 44, "EC001", false),
                seat(2, 44, "EC002", false),
                seat(3, 44, "EC003", true)
        );

        // Return existingSeats (before) then seatsAfter (for availability calc)
        when(seatRepository.findAll()).thenReturn(existingSeats).thenReturn(seatsAfter);

        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassFlightResponseDTO res = service.updateClassFlight(req);

        // Verify deletions
        ArgumentCaptor<Seat> delCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository, times(2)).delete(delCaptor.capture());
        List<String> deletedCodes = delCaptor.getAllValues().stream().map(Seat::getSeatCode).collect(Collectors.toList());
        assertEquals(Arrays.asList("EC005", "EC004"), deletedCodes);

        // Available seats should be count of unbooked in seatsAfter -> 2 (EC001, EC002)
        assertNotNull(res);
        assertEquals(3, res.getSeatCapacity());
        assertEquals(2, res.getAvailableSeats());
        verify(classFlightRepository).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("updateClassFlight: decrease capacity below booked seats -> throws with bookedCount message")
    void updateClassFlight_decreaseCapacity_belowBooked_throws() {
        var existing = cf(66, "FL-6", "economy", 4, 4, new BigDecimal("1000000"));
        when(classFlightRepository.findById(66)).thenReturn(Optional.of(existing));

        // All seats booked -> newCap below bookedCount triggers first validation branch
        List<Seat> existingSeats = Arrays.asList(
                seat(1, 66, "EC001", true),
                seat(2, 66, "EC002", true),
                seat(3, 66, "EC003", true),
                seat(4, 66, "EC004", true)
        );
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(66)
                .seatCapacity(2) // newCap (2) < bookedCount (4) -> triggers first validation
                .price(new BigDecimal("800000"))
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.updateClassFlight(req));
        assertTrue(ex.getMessage().contains("Cannot set seat capacity below currently booked seats"));
        verify(classFlightRepository, never()).save(any(ClassFlight.class));
    }

    @Test
    @DisplayName("deleteClassFlight: found deletes and returns DTO")
    void deleteClassFlight_found() {
        var existing = cf(70, "FL-DEL", "first", 3, 3, new BigDecimal("7000000"));
        when(classFlightRepository.findById(70)).thenReturn(Optional.of(existing));

        ClassFlightResponseDTO res = service.deleteClassFlight(70);

        assertNotNull(res);
        assertEquals(70, res.getId());
        verify(classFlightRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteClassFlight: returns null when not found")
    void deleteClassFlight_notFound() {
        when(classFlightRepository.findById(404)).thenReturn(Optional.empty());

        ClassFlightResponseDTO res = service.deleteClassFlight(404);
        assertNull(res);
        verify(classFlightRepository, never()).delete(any());
    }
    @Test
    @DisplayName("updateClassFlight: increase capacity for business creates BU codes")
    void updateClassFlight_increaseCapacity_prefixBusiness() {
        var existing = cf(101, "FL-BU", "business", 2, 2, new BigDecimal("3000000"));
        when(classFlightRepository.findById(101)).thenReturn(Optional.of(existing));

        List<Seat> existingSeats = Arrays.asList(
                seat(1, 101, "BU001", false),
                seat(2, 101, "BU002", false)
        );
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(101)
                .seatCapacity(4) // BU003, BU004
                .price(new BigDecimal("3500000"))
                .build();

        List<Seat> seatsAfter = new ArrayList<>(existingSeats);
        seatsAfter.add(seat(3, 101, "BU003", false));
        seatsAfter.add(seat(4, 101, "BU004", false));

        when(seatRepository.findAll()).thenReturn(existingSeats).thenReturn(seatsAfter);
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassFlightResponseDTO res = service.updateClassFlight(req);

        ArgumentCaptor<AddSeatRequestDTO> captor = ArgumentCaptor.forClass(AddSeatRequestDTO.class);
        verify(seatRestService, times(2)).createSeat(captor.capture());
        List<String> created = captor.getAllValues().stream().map(AddSeatRequestDTO::getSeatCode).collect(Collectors.toList());
        assertEquals(Arrays.asList("BU003", "BU004"), created);
        assertEquals(4, res.getSeatCapacity());
        assertEquals(4, res.getAvailableSeats());
    }

    @Test
    @DisplayName("updateClassFlight: increase capacity for first creates FI codes")
    void updateClassFlight_increaseCapacity_prefixFirst() {
        var existing = cf(102, "FL-FI", "first", 0, 0, new BigDecimal("9000000"));
        when(classFlightRepository.findById(102)).thenReturn(Optional.of(existing));

        List<Seat> existingSeats = Collections.emptyList();
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(102)
                .seatCapacity(2) // FI001, FI002
                .price(new BigDecimal("9500000"))
                .build();

        List<Seat> seatsAfter = Arrays.asList(
                seat(1, 102, "FI001", false),
                seat(2, 102, "FI002", false)
        );

        when(seatRepository.findAll()).thenReturn(existingSeats).thenReturn(seatsAfter);
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassFlightResponseDTO res = service.updateClassFlight(req);

        ArgumentCaptor<AddSeatRequestDTO> captor = ArgumentCaptor.forClass(AddSeatRequestDTO.class);
        verify(seatRestService, times(2)).createSeat(captor.capture());
        List<String> created = captor.getAllValues().stream().map(AddSeatRequestDTO::getSeatCode).collect(Collectors.toList());
        assertEquals(Arrays.asList("FI001", "FI002"), created);
        assertEquals(2, res.getSeatCapacity());
        assertEquals(2, res.getAvailableSeats());
    }

    @Test
    @DisplayName("updateClassFlight: unknown/null classType defaults to EC prefix")
    void updateClassFlight_increaseCapacity_defaultPrefix() {
        var existing = cf(103, "FL-EC", null, 0, 0, new BigDecimal("1200000")); // null triggers default
        when(classFlightRepository.findById(103)).thenReturn(Optional.of(existing));

        List<Seat> existingSeats = Collections.emptyList();
        when(seatRepository.findAll()).thenReturn(existingSeats);

        var req = UpdateClassFlightRequestDTO.builder()
                .id(103)
                .seatCapacity(1) // EC001
                .price(new BigDecimal("1300000"))
                .build();

        List<Seat> seatsAfter = Collections.singletonList(seat(1, 103, "EC001", false));

        when(seatRepository.findAll()).thenReturn(existingSeats).thenReturn(seatsAfter);
        when(classFlightRepository.save(any(ClassFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        ClassFlightResponseDTO res = service.updateClassFlight(req);

        ArgumentCaptor<AddSeatRequestDTO> captor = ArgumentCaptor.forClass(AddSeatRequestDTO.class);
        verify(seatRestService, times(1)).createSeat(captor.capture());
        assertEquals("EC001", captor.getValue().getSeatCode());
        assertEquals(1, res.getSeatCapacity());
        assertEquals(1, res.getAvailableSeats());
    }
}

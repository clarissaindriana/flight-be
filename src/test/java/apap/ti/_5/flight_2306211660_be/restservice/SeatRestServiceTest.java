package apap.ti._5.flight_2306211660_be.restservice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.ClassFlight;
import apap.ti._5.flight_2306211660_be.model.Seat;
import apap.ti._5.flight_2306211660_be.repository.ClassFlightRepository;
import apap.ti._5.flight_2306211660_be.repository.SeatRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.UpdateSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SeatRestServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ClassFlightRepository classFlightRepository;

    @InjectMocks
    private SeatRestServiceImpl seatRestService;

    private Seat seat(Integer id, Integer classFlightId, String code, boolean booked, UUID passengerId) {
        return Seat.builder()
                .id(id)
                .classFlightId(classFlightId)
                .seatCode(code)
                .isBooked(booked)
                .passengerId(passengerId)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private ClassFlight cf(Integer id, String flightId, String classType, int cap, int avail) {
        return ClassFlight.builder()
                .id(id)
                .flightId(flightId)
                .classType(classType)
                .seatCapacity(cap)
                .availableSeats(avail)
                .build();
    }

    @Test
    void createSeat_success() {
        var req = AddSeatRequestDTO.builder()
                .classFlightId(10)
                .seatCode("EC001")
                .build();

        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        SeatResponseDTO res = seatRestService.createSeat(req);

        assertNotNull(res);
        assertEquals("EC001", res.getSeatCode());
        assertEquals(10, res.getClassFlightId());
        assertFalse(res.getIsBooked());
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void getAllSeats_success() {
        var s1 = seat(1, 10, "EC001", false, null);
        var s2 = seat(2, 10, "EC002", true, UUID.randomUUID());
        when(seatRepository.findAll()).thenReturn(List.of(s1, s2));

        List<SeatResponseDTO> list = seatRestService.getAllSeats();

        assertEquals(2, list.size());
        assertEquals("EC001", list.get(0).getSeatCode());
        assertEquals("EC002", list.get(1).getSeatCode());
        verify(seatRepository).findAll();
    }

    @Test
    void getSeatsByClassFlight_filtersAndSorts() {
        var s3 = seat(3, 10, "EC003", false, null);
        var s1 = seat(1, 10, "EC001", false, null);
        var s2 = seat(2, 10, "EC002", false, null);
        var otherClass = seat(9, 11, "EC009", false, null);
        when(seatRepository.findAll()).thenReturn(Arrays.asList(s3, s1, s2, otherClass));

        var res = seatRestService.getSeatsByClassFlight(10);

        assertEquals(3, res.size());
        assertEquals("EC001", res.get(0).getSeatCode());
        assertEquals("EC002", res.get(1).getSeatCode());
        assertEquals("EC003", res.get(2).getSeatCode());
    }

    @Test
    void getSeat_found() {
        var s = seat(7, 12, "BU001", false, null);
        when(seatRepository.findById(7)).thenReturn(Optional.of(s));

        var res = seatRestService.getSeat(7);

        assertNotNull(res);
        assertEquals("BU001", res.getSeatCode());
        verify(seatRepository).findById(7);
    }

    @Test
    void getSeat_notFound_returnsNull() {
        when(seatRepository.findById(77)).thenReturn(Optional.empty());

        var res = seatRestService.getSeat(77);

        assertNull(res);
    }

    @Test
    void updateSeat_found_updatesPassengerAndBooked() {
        var existing = seat(5, 10, "EC005", false, null);
        UUID newPid = UUID.randomUUID();
        var req = UpdateSeatRequestDTO.builder()
                .id(5)
                .passengerId(newPid)
                .build();

        when(seatRepository.findById(5)).thenReturn(Optional.of(existing));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        SeatResponseDTO res = seatRestService.updateSeat(req);

        assertNotNull(res);
        assertEquals(newPid, res.getPassengerId());
        assertTrue(res.getIsBooked());
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void updateSeat_notFound_returnsNull() {
        var req = UpdateSeatRequestDTO.builder()
                .id(404)
                .passengerId(UUID.randomUUID())
                .build();
        when(seatRepository.findById(404)).thenReturn(Optional.empty());

        SeatResponseDTO res = seatRestService.updateSeat(req);

        assertNull(res);
        verify(seatRepository, never()).save(any());
    }

    @Test
    void deleteSeat_found_returnsDTO() {
        var existing = seat(8, 10, "EC008", false, null);
        when(seatRepository.findById(8)).thenReturn(Optional.of(existing));
        doNothing().when(seatRepository).delete(existing);

        var res = seatRestService.deleteSeat(8);

        assertNotNull(res);
        assertEquals("EC008", res.getSeatCode());
        verify(seatRepository).delete(existing);
    }

    @Test
    void deleteSeat_notFound_returnsNull() {
        when(seatRepository.findById(9)).thenReturn(Optional.empty());

        var res = seatRestService.deleteSeat(9);

        assertNull(res);
        verify(seatRepository, never()).delete(any());
    }

    @Test
    void getSeatsByFlight_aggregatesFromClassFlightsAndSorts() {
        var cf1 = cf(100, "FL-1", "economy", 10, 10);
        var cf2 = cf(101, "FL-2", "business", 5, 5);
        var cf3 = cf(102, "FL-1", "business", 7, 7);

        when(classFlightRepository.findAll()).thenReturn(List.of(cf1, cf2, cf3));

        var s1 = seat(1, 100, "EC003", false, null);
        var s2 = seat(2, 100, "EC001", false, null);
        var s3 = seat(3, 102, "BU002", false, null);
        var s4 = seat(4, 102, "BU001", false, null);
        var otherFlightSeat = seat(9, 101, "BU999", false, null);

        when(seatRepository.findAll()).thenReturn(List.of(s1, s2, s3, s4, otherFlightSeat));

        var res = seatRestService.getSeatsByFlight("FL-1");

        assertEquals(4, res.size());
        // Sorted by seatCode ascending across collected seats
        assertEquals("BU001", res.get(0).getSeatCode());
        assertEquals("BU002", res.get(1).getSeatCode());
        assertEquals("EC001", res.get(2).getSeatCode());
        assertEquals("EC003", res.get(3).getSeatCode());
    }
}

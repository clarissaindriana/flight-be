package apap.ti._5.flight_2306211660_be.restservice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.BookingPassengerId;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.AddBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.UpdateBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger.BookingPassengerResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.bookingPassenger.BookingPassengerRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BookingPassengerRestServiceTest {

    @Mock
    private BookingPassengerRepository bookingPassengerRepository;

    @InjectMocks
    private BookingPassengerRestServiceImpl bookingPassengerRestService;

    private BookingPassenger build(String bookingId, UUID passengerId, LocalDateTime createdAt) {
        return BookingPassenger.builder()
                .bookingId(bookingId)
                .passengerId(passengerId)
                .createdAt(createdAt)
                .build();
    }

    @Test
    void createBookingPassenger_success() {
        var dto = AddBookingPassengerRequestDTO.builder()
                .bookingId("FLT-001-001")
                .passengerId(UUID.randomUUID())
                .build();

        when(bookingPassengerRepository.save(any(BookingPassenger.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BookingPassengerResponseDTO res = bookingPassengerRestService.createBookingPassenger(dto);

        assertNotNull(res);
        assertEquals(dto.getBookingId(), res.getBookingId());
        assertEquals(dto.getPassengerId(), res.getPassengerId());

        ArgumentCaptor<BookingPassenger> captor = ArgumentCaptor.forClass(BookingPassenger.class);
        verify(bookingPassengerRepository).save(captor.capture());
        assertEquals(dto.getBookingId(), captor.getValue().getBookingId());
        assertEquals(dto.getPassengerId(), captor.getValue().getPassengerId());
    }

    @Test
    void getAllBookingPassengers_success() {
        var p1 = build("B-1", UUID.randomUUID(), LocalDateTime.now().minusDays(2));
        var p2 = build("B-2", UUID.randomUUID(), LocalDateTime.now().minusDays(1));
        when(bookingPassengerRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<BookingPassengerResponseDTO> res = bookingPassengerRestService.getAllBookingPassengers();

        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("B-1", res.get(0).getBookingId());
        assertEquals("B-2", res.get(1).getBookingId());
        verify(bookingPassengerRepository).findAll();
    }

    @Test
    void getBookingPassengersByBooking_filters() {
        var idMatch = UUID.randomUUID();
        var keep = build("B-99", idMatch, LocalDateTime.now());
        var other = build("B-1", UUID.randomUUID(), LocalDateTime.now());
        when(bookingPassengerRepository.findAll()).thenReturn(Arrays.asList(keep, other));

        var res = bookingPassengerRestService.getBookingPassengersByBooking("B-99");

        assertEquals(1, res.size());
        assertEquals("B-99", res.get(0).getBookingId());
    }

    @Test
    void getBookingPassengersByPassenger_filters() {
        var target = UUID.randomUUID();
        var keep = build("B-2", target, LocalDateTime.now());
        var other = build("B-3", UUID.randomUUID(), LocalDateTime.now());
        when(bookingPassengerRepository.findAll()).thenReturn(Arrays.asList(keep, other));

        var res = bookingPassengerRestService.getBookingPassengersByPassenger(target);

        assertEquals(1, res.size());
        assertEquals(target, res.get(0).getPassengerId());
    }

    @Test
    void getBookingPassenger_found() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();
        var entity = build(bookingId, passengerId, LocalDateTime.now());

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.of(entity));

        BookingPassengerResponseDTO res = bookingPassengerRestService.getBookingPassenger(bookingId, passengerId);

        assertNotNull(res);
        assertEquals(bookingId, res.getBookingId());
        assertEquals(passengerId, res.getPassengerId());
    }

    @Test
    void getBookingPassenger_notFound_returnsNull() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(bookingPassengerRestService.getBookingPassenger(bookingId, passengerId));
    }

    @Test
    void updateBookingPassenger_found_returnsSaved() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();
        var existing = build(bookingId, passengerId, LocalDateTime.now().minusDays(3));

        var dto = UpdateBookingPassengerRequestDTO.builder()
                .bookingId(bookingId)
                .passengerId(passengerId)
                .build();

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookingPassengerRepository.save(any(BookingPassenger.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingPassengerResponseDTO res = bookingPassengerRestService.updateBookingPassenger(dto);

        assertNotNull(res);
        assertEquals(bookingId, res.getBookingId());
        assertEquals(passengerId, res.getPassengerId());
        verify(bookingPassengerRepository).save(existing);
    }

    @Test
    void updateBookingPassenger_notFound_returnsNull() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();

        var dto = UpdateBookingPassengerRequestDTO.builder()
                .bookingId(bookingId)
                .passengerId(passengerId)
                .build();

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(bookingPassengerRestService.updateBookingPassenger(dto));
        verify(bookingPassengerRepository, never()).save(any());
    }

    @Test
    void deleteBookingPassenger_found() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();
        var existing = build(bookingId, passengerId, LocalDateTime.now().minusDays(5));

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(bookingPassengerRepository).delete(existing);

        BookingPassengerResponseDTO res = bookingPassengerRestService.deleteBookingPassenger(bookingId, passengerId);

        assertNotNull(res);
        assertEquals(bookingId, res.getBookingId());
        assertEquals(passengerId, res.getPassengerId());
        verify(bookingPassengerRepository).delete(existing);
    }

    @Test
    void deleteBookingPassenger_notFound_returnsNull() {
        String bookingId = "B-1";
        UUID passengerId = UUID.randomUUID();

        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        when(bookingPassengerRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(bookingPassengerRestService.deleteBookingPassenger(bookingId, passengerId));
        verify(bookingPassengerRepository, never()).delete(any());
    }
}

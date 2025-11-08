package apap.ti._5.flight_2306211660_be.restservice.booking;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;

public interface BookingRestService {

    BookingResponseDTO createBooking(AddBookingRequestDTO dto);

    // Existing default (active only)
    List<BookingResponseDTO> getAllBookings();

    // New: includeDeleted toggle
    List<BookingResponseDTO> getAllBookings(Boolean includeDeleted);

    // Existing default (active only)
    List<BookingResponseDTO> getBookingsByFlight(String flightId);

    // New: includeDeleted toggle
    List<BookingResponseDTO> getBookingsByFlight(String flightId, Boolean includeDeleted);

    BookingResponseDTO getBooking(String id);

    BookingResponseDTO updateBooking(UpdateBookingRequestDTO dto);

    BookingResponseDTO deleteBooking(String id);
}

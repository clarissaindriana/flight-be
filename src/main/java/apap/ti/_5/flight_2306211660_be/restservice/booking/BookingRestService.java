package apap.ti._5.flight_2306211660_be.restservice.booking;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingChartResultDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;

public interface BookingRestService {

    BookingResponseDTO createBooking(AddBookingRequestDTO dto);

    // Existing default (active only)
    List<BookingResponseDTO> getAllBookings();

    // New: includeDeleted toggle
    List<BookingResponseDTO> getAllBookings(Boolean includeDeleted);

    // New: includeDeleted toggle with search and filters
    List<BookingResponseDTO> getAllBookings(Boolean includeDeleted, String search, String contactEmail, Integer status);

    // Existing default (active only)
    List<BookingResponseDTO> getBookingsByFlight(String flightId);

    // New: includeDeleted toggle
    List<BookingResponseDTO> getBookingsByFlight(String flightId, Boolean includeDeleted);

    // New: includeDeleted toggle with search and filters
    List<BookingResponseDTO> getBookingsByFlight(String flightId, Boolean includeDeleted, String search, String contactEmail, Integer status);

    BookingResponseDTO getBooking(String id);

    BookingResponseDTO updateBooking(UpdateBookingRequestDTO dto);

    BookingResponseDTO deleteBooking(String id);

    // Count bookings created today (isDeleted = FALSE)
    long getTodayBookings();

    // Chart statistics per flight for a given month & year (counts only Paid(2) and Unpaid(1))
    java.util.List<BookingChartResponseDTO> getBookingChart(int month, int year);
 
    // New: return chart list + summary (totalBookings, totalRevenue, topPerformer)
    BookingChartResultDTO getBookingChartData(int month, int year);

    // Internal: confirm payment callback from Bill service to set booking as Paid
    BookingResponseDTO confirmPayment(ConfirmPaymentRequestDTO dto);
}

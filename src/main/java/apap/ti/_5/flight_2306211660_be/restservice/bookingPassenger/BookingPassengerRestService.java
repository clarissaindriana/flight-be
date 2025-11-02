package apap.ti._5.flight_2306211660_be.restservice.bookingPassenger;

import java.util.List;
import java.util.UUID;

import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.AddBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.UpdateBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger.BookingPassengerResponseDTO;

public interface BookingPassengerRestService {

    BookingPassengerResponseDTO createBookingPassenger(AddBookingPassengerRequestDTO dto);

    List<BookingPassengerResponseDTO> getAllBookingPassengers();

    List<BookingPassengerResponseDTO> getBookingPassengersByBooking(String bookingId);

    List<BookingPassengerResponseDTO> getBookingPassengersByPassenger(UUID passengerId);

    BookingPassengerResponseDTO getBookingPassenger(String bookingId, UUID passengerId);

    BookingPassengerResponseDTO updateBookingPassenger(UpdateBookingPassengerRequestDTO dto);

    BookingPassengerResponseDTO deleteBookingPassenger(String bookingId, UUID passengerId);
}

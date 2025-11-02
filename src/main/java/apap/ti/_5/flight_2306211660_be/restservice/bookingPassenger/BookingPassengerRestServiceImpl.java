package apap.ti._5.flight_2306211660_be.restservice.bookingPassenger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import apap.ti._5.flight_2306211660_be.model.BookingPassenger;
import apap.ti._5.flight_2306211660_be.model.BookingPassengerId;
import apap.ti._5.flight_2306211660_be.repository.BookingPassengerRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.AddBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.UpdateBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger.BookingPassengerResponseDTO;

@Service
public class BookingPassengerRestServiceImpl implements BookingPassengerRestService {

    @Autowired
    private BookingPassengerRepository bookingPassengerRepository;

    @Override
    public BookingPassengerResponseDTO createBookingPassenger(AddBookingPassengerRequestDTO dto) {
        BookingPassengerId id = new BookingPassengerId(dto.getBookingId(), dto.getPassengerId());

        BookingPassenger bookingPassenger = BookingPassenger.builder()
                .bookingId(dto.getBookingId())
                .passengerId(dto.getPassengerId())
                .build();

        bookingPassenger = bookingPassengerRepository.save(bookingPassenger);
        return convertToBookingPassengerResponseDTO(bookingPassenger);
    }

    @Override
    public List<BookingPassengerResponseDTO> getAllBookingPassengers() {
        List<BookingPassenger> bookingPassengers = bookingPassengerRepository.findAll();
        return bookingPassengers.stream()
                .map(this::convertToBookingPassengerResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingPassengerResponseDTO> getBookingPassengersByBooking(String bookingId) {
        // This would require a custom query method in the repository
        // For now, we'll filter from all results
        return getAllBookingPassengers().stream()
                .filter(bp -> bp.getBookingId().equals(bookingId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingPassengerResponseDTO> getBookingPassengersByPassenger(UUID passengerId) {
        // This would require a custom query method in the repository
        // For now, we'll filter from all results
        return getAllBookingPassengers().stream()
                .filter(bp -> bp.getPassengerId().equals(passengerId))
                .collect(Collectors.toList());
    }

    @Override
    public BookingPassengerResponseDTO getBookingPassenger(String bookingId, UUID passengerId) {
        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        BookingPassenger bookingPassenger = bookingPassengerRepository.findById(id).orElse(null);
        if (bookingPassenger == null) {
            return null;
        }
        return convertToBookingPassengerResponseDTO(bookingPassenger);
    }

    @Override
    public BookingPassengerResponseDTO updateBookingPassenger(UpdateBookingPassengerRequestDTO dto) {
        BookingPassengerId id = new BookingPassengerId(dto.getBookingId(), dto.getPassengerId());
        BookingPassenger bookingPassenger = bookingPassengerRepository.findById(id).orElse(null);
        if (bookingPassenger == null) {
            return null;
        }

        // For this junction table, there's not much to update since it's just the relationship
        // In a real scenario, you might have additional fields like seat assignments
        bookingPassenger = bookingPassengerRepository.save(bookingPassenger);
        return convertToBookingPassengerResponseDTO(bookingPassenger);
    }

    @Override
    public BookingPassengerResponseDTO deleteBookingPassenger(String bookingId, UUID passengerId) {
        BookingPassengerId id = new BookingPassengerId(bookingId, passengerId);
        BookingPassenger bookingPassenger = bookingPassengerRepository.findById(id).orElse(null);
        if (bookingPassenger == null) {
            return null;
        }

        bookingPassengerRepository.delete(bookingPassenger);
        return convertToBookingPassengerResponseDTO(bookingPassenger);
    }

    private BookingPassengerResponseDTO convertToBookingPassengerResponseDTO(BookingPassenger bookingPassenger) {
        return BookingPassengerResponseDTO.builder()
                .bookingId(bookingPassenger.getBookingId())
                .passengerId(bookingPassenger.getPassengerId())
                .createdAt(bookingPassenger.getCreatedAt())
                .build();
    }
}

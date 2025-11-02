package apap.ti._5.flight_2306211660_be.restcontroller.bookingPassenger;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.AddBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger.UpdateBookingPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger.BookingPassengerResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.bookingPassenger.BookingPassengerRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BookingPassengerRestController {
    @Autowired
    private BookingPassengerRestService bookingPassengerRestService;

    public static final String BASE_URL = "/booking-passenger";
    public static final String VIEW_BOOKING_PASSENGER = BASE_URL + "/{bookingId}/{passengerId}";
    public static final String CREATE_BOOKING_PASSENGER = BASE_URL + "/create";
    public static final String UPDATE_BOOKING_PASSENGER = BASE_URL + "/update";
    public static final String DELETE_BOOKING_PASSENGER = BASE_URL + "/delete/{bookingId}/{passengerId}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<BookingPassengerResponseDTO>>> getAllBookingPassengers() {
        var baseResponseDTO = new BaseResponseDTO<List<BookingPassengerResponseDTO>>();

        List<BookingPassengerResponseDTO> bookingPassengers = bookingPassengerRestService.getAllBookingPassengers();

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(bookingPassengers);
        baseResponseDTO.setMessage("Data Booking Passenger Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_BOOKING_PASSENGER)
    public ResponseEntity<BaseResponseDTO<BookingPassengerResponseDTO>> getBookingPassenger(
            @PathVariable String bookingId, @PathVariable UUID passengerId) {
        var baseResponseDTO = new BaseResponseDTO<BookingPassengerResponseDTO>();

        BookingPassengerResponseDTO bookingPassenger = bookingPassengerRestService.getBookingPassenger(bookingId, passengerId);

        if (bookingPassenger == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Booking Passenger Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(bookingPassenger);
        baseResponseDTO.setMessage("Data Booking Passenger Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_BOOKING_PASSENGER)
    public ResponseEntity<BaseResponseDTO<BookingPassengerResponseDTO>> createBookingPassenger(
            @Valid @RequestBody AddBookingPassengerRequestDTO addBookingPassengerRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<BookingPassengerResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            BookingPassengerResponseDTO bookingPassenger = bookingPassengerRestService.createBookingPassenger(addBookingPassengerRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(bookingPassenger);
            baseResponseDTO.setMessage("Data Booking Passenger Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_BOOKING_PASSENGER)
    public ResponseEntity<BaseResponseDTO<BookingPassengerResponseDTO>> updateBookingPassenger(
            @Valid @RequestBody UpdateBookingPassengerRequestDTO updateBookingPassengerRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<BookingPassengerResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            BookingPassengerResponseDTO bookingPassenger = bookingPassengerRestService.updateBookingPassenger(updateBookingPassengerRequestDTO);

            if (bookingPassenger == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Passenger Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(bookingPassenger);
            baseResponseDTO.setMessage("Data Booking Passenger Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(DELETE_BOOKING_PASSENGER)
    public ResponseEntity<BaseResponseDTO<BookingPassengerResponseDTO>> deleteBookingPassenger(
            @PathVariable String bookingId, @PathVariable UUID passengerId) {
        var baseResponseDTO = new BaseResponseDTO<BookingPassengerResponseDTO>();

        try {
            BookingPassengerResponseDTO bookingPassenger = bookingPassengerRestService.deleteBookingPassenger(bookingId, passengerId);

            if (bookingPassenger == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Passenger Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(bookingPassenger);
            baseResponseDTO.setMessage("Data Booking Passenger Berhasil Dihapus");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

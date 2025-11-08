package apap.ti._5.flight_2306211660_be.restcontroller.booking;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;

import apap.ti._5.flight_2306211660_be.model.Booking;
import apap.ti._5.flight_2306211660_be.repository.BookingRepository;
import apap.ti._5.flight_2306211660_be.restservice.booking.BookingRestService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookingRestController {
    @Autowired
    private BookingRestService bookingRestService;

    @Autowired
    private BookingRepository bookingRepository;

    public static final String BASE_URL = "/booking";
    public static final String VIEW_BOOKING = BASE_URL + "/{id}";
    public static final String CREATE_BOOKING = BASE_URL + "/create";
    public static final String UPDATE_BOOKING = BASE_URL + "/update";
    public static final String DELETE_BOOKING = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<BookingResponseDTO>>> getAllBookings(
            @RequestParam(required = false) String flightId,
            @RequestParam(required = false) Boolean includeDeleted) {
        var baseResponseDTO = new BaseResponseDTO<List<BookingResponseDTO>>();

        List<BookingResponseDTO> bookings;

        if (flightId != null) {
            bookings = bookingRestService.getBookingsByFlight(flightId, includeDeleted);
        } else {
            // Default: show active only when includeDeleted is null/false; include archives when true
            bookings = bookingRestService.getAllBookings(includeDeleted);
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(bookings);
        baseResponseDTO.setMessage("Data Booking Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> getBooking(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

        BookingResponseDTO booking = bookingRestService.getBooking(id);

        if (booking == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Booking Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(booking);
        baseResponseDTO.setMessage("Data Booking Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> createBooking(
            @Valid @RequestBody AddBookingRequestDTO addBookingRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

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
            BookingResponseDTO booking = bookingRestService.createBooking(addBookingRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(booking);
            baseResponseDTO.setMessage("Data Booking Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (IllegalArgumentException ex) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> updateBooking(
            @Valid @RequestBody UpdateBookingRequestDTO updateBookingRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

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
            BookingResponseDTO booking = bookingRestService.updateBooking(updateBookingRequestDTO);

            if (booking == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(booking);
            baseResponseDTO.setMessage("Data Booking Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
    
    // GET /api/booking/statistics?start=YYYY-MM-DD&end=YYYY-MM-DD
    @GetMapping(BASE_URL + "/statistics")
    public ResponseEntity<BaseResponseDTO<List<Map<String, Object>>>> getBookingStatistics(
            @RequestParam String start,
            @RequestParam String end) {
        var baseResponseDTO = new BaseResponseDTO<List<Map<String, Object>>>();
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            LocalDateTime startDt = startDate.atStartOfDay();
            LocalDateTime endDt = endDate.atTime(23, 59, 59);

            // Filter only Unpaid(1) and Paid(2), not soft-deleted, within period by createdAt
            List<Booking> filtered = bookingRepository.findAll().stream()
                    .filter(b -> b.getIsDeleted() != null && !b.getIsDeleted())
                    .filter(b -> b.getStatus() != null && (b.getStatus() == 1 || b.getStatus() == 2))
                    .filter(b -> {
                        LocalDateTime c = b.getCreatedAt();
                        return c != null && !c.isBefore(startDt) && !c.isAfter(endDt);
                    })
                    .collect(Collectors.toList());

            // Group by flightId and compute bookingCount + totalRevenue
            List<Map<String, Object>> stats = filtered.stream()
                    .collect(Collectors.groupingBy(Booking::getFlightId))
                    .entrySet().stream()
                    .map(e -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("flightId", e.getKey());
                        m.put("bookingCount", e.getValue().size());
                        BigDecimal totalRevenue = e.getValue().stream()
                                .map(Booking::getTotalPrice)
                                .filter(v -> v != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        m.put("totalRevenue", totalRevenue);
                        return m;
                    })
                    .sorted(Comparator.comparing(m -> (String) m.get("flightId")))
                    .collect(Collectors.toList());

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(stats);
            baseResponseDTO.setMessage("Booking statistics calculated successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Invalid request: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(DELETE_BOOKING)
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> deleteBooking(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

        try {
            BookingResponseDTO booking = bookingRestService.deleteBooking(id);

            if (booking == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Booking Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(booking);
            baseResponseDTO.setMessage("Data Booking Berhasil Dihapus");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalStateException ex) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

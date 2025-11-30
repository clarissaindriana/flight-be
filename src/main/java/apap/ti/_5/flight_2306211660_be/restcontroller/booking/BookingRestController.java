package apap.ti._5.flight_2306211660_be.restcontroller.booking;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.AddBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.UpdateBookingRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.booking.BookingResponseDTO;

import apap.ti._5.flight_2306211660_be.model.Bill;
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
import apap.ti._5.flight_2306211660_be.config.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class BookingRestController {
    @Autowired
    private BookingRestService bookingRestService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private apap.ti._5.flight_2306211660_be.restservice.bill.BillRestService billRestService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BookingRestController.class);

    public static final String BASE_URL = "/booking";
    public static final String VIEW_BOOKING = BASE_URL + "/{id}";
    public static final String CREATE_BOOKING = BASE_URL + "/create";
    public static final String UPDATE_BOOKING = BASE_URL + "/update";
    public static final String DELETE_BOOKING = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<List<BookingResponseDTO>>> getAllBookings(
            @RequestParam(required = false) String flightId,
            @RequestParam(required = false) Boolean includeDeleted,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) Integer status) {
        var baseResponseDTO = new BaseResponseDTO<List<BookingResponseDTO>>();

        List<BookingResponseDTO> bookings;

        // includeDeleted may only be requested by SUPERADMIN or FLIGHT_AIRLINE
        if (includeDeleted != null && includeDeleted) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean allowed = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN") || a.getAuthority().equals("ROLE_FLIGHT_AIRLINE"));
            if (!allowed) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("Access denied: includeDeleted filter is restricted");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }
        }

        if (flightId != null) {
            bookings = bookingRestService.getBookingsByFlight(flightId, includeDeleted, search, contactEmail, status);
        } else {
            // Default: show active only when includeDeleted is null/false; include archives when true
            bookings = bookingRestService.getAllBookings(includeDeleted, search, contactEmail, status);
        }

        // Ownership enforcement: if current user is CUSTOMER, only return bookings with contactEmail == user's email
        String role = CurrentUser.getRole();
        if (role != null && role.contains("ROLE_CUSTOMER")) {
            String email = CurrentUser.getEmail();
            if (email != null) {
                bookings = bookings.stream()
                        .filter(b -> email.equalsIgnoreCase(b.getContactEmail()))
                        .toList();
            } else {
                bookings = List.of();
            }
        }

        // Adjust booking status based on bill status
        if (role != null && role.contains("ROLE_CUSTOMER")) {
            // For customers, fetch their bills and adjust booking statuses
            String customerId = CurrentUser.getUserId();
            if (customerId != null) {
                List<Bill> bills = billRestService.getCustomerBills(customerId, null, null, null);
                adjustBookingStatuses(bookings, bills);
            }
        } else if (role != null && (role.contains("ROLE_SUPERADMIN") || role.contains("ROLE_FLIGHT_AIRLINE"))) {
            // For superadmin or flight_airline, fetch all flight bills and adjust all booking statuses
            List<Bill> bills = billRestService.getServiceBills("Flight", null, null);
            adjustBookingStatuses(bookings, bills);
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(bookings);
        baseResponseDTO.setMessage("Data Booking Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_BOOKING)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> getBooking(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

        BookingResponseDTO booking = bookingRestService.getBooking(id);

        if (booking == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Booking Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }

        // If customer, enforce ownership
        String role = CurrentUser.getRole();
        if (role != null && role.contains("ROLE_CUSTOMER")) {
            String email = CurrentUser.getEmail();
            if (email == null || !email.equalsIgnoreCase(booking.getContactEmail())) {
                baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                baseResponseDTO.setMessage("Access denied");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
            }
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(booking);
        baseResponseDTO.setMessage("Data Booking Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_BOOKING)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN')")
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
            // If customer, force contactEmail to current user's email
            String role = CurrentUser.getRole();
            if (role != null && role.contains("ROLE_CUSTOMER")) {
                String email = CurrentUser.getEmail();
                if (email != null) {
                    addBookingRequestDTO.setContactEmail(email);
                }
            }
            BookingResponseDTO booking = bookingRestService.createBooking(addBookingRequestDTO);

            // Auto-create bill for this booking (best-effort). Use CurrentUser.getUserId() as customerId when available.
            try {
                String customerId = CurrentUser.getUserId();
                if (customerId == null || customerId.isBlank()) customerId = booking.getContactEmail();

                AddBillRequestDTO billReq = new AddBillRequestDTO();
                billReq.setCustomerId(customerId);
                billReq.setServiceName("Flight");
                billReq.setServiceReferenceId(booking.getId());
                String desc = "Booking Flight " + (booking.getRoute() != null ? booking.getRoute() : booking.getId()) + " - " + (booking.getClassType() != null ? booking.getClassType() : "");
                billReq.setDescription(desc);
                billReq.setAmount(booking.getTotalPrice() != null ? booking.getTotalPrice() : java.math.BigDecimal.ZERO);

                billRestService.createBill(billReq);
            } catch (Exception e) {
                logger.warn("Failed to create bill for booking {}: {}", booking.getId(), e.getMessage());
            }

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
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN')")
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
            // If customer, ensure ownership before updating
            String role = CurrentUser.getRole();
            if (role != null && role.contains("ROLE_CUSTOMER")) {
                String email = CurrentUser.getEmail();
                BookingResponseDTO existing = bookingRestService.getBooking(updateBookingRequestDTO.getId());
                if (existing == null) {
                    var base = new BaseResponseDTO<BookingResponseDTO>();
                    base.setStatus(HttpStatus.NOT_FOUND.value());
                    base.setMessage("Booking Tidak Ditemukan");
                    base.setTimestamp(new Date());
                    return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
                }
                if (email == null || !email.equalsIgnoreCase(existing.getContactEmail())) {
                    var base = new BaseResponseDTO<BookingResponseDTO>();
                    base.setStatus(HttpStatus.FORBIDDEN.value());
                    base.setMessage("Access denied");
                    base.setTimestamp(new Date());
                    return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
                }
            }

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
    // Note: statistics endpoint removed in favor of unified /chart service

    @GetMapping(BASE_URL + "/today")
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<Long>> getTodayBookings() {
        var base = new BaseResponseDTO<Long>();
        try {
            long total = bookingRestService.getTodayBookings();
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Total bookings today retrieved");
            base.setData(total);
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(BASE_URL + "/chart")
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<Object>> getBookingChart(
            @RequestParam int month,
            @RequestParam int year) {
        var base = new BaseResponseDTO<Object>();
        try {
            var result = bookingRestService.getBookingChartData(month, year);
            // If no chart data for the period, return HTTP 200 with empty list as required
            if (result.getChart() == null || result.getChart().isEmpty()) {
                base.setStatus(HttpStatus.OK.value());
                base.setData(java.util.Collections.emptyList());
                base.setMessage("No booking data for the selected period");
                base.setTimestamp(new Date());
                return ResponseEntity.ok(base);
            }

            // Build response map when we have data
            Map<String, Object> data = new HashMap<>();
            data.put("chart", result.getChart());
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalBookings", result.getSummary() != null ? result.getSummary().getTotalBookings() : 0L);
            summary.put("totalRevenue", result.getSummary() != null ? result.getSummary().getTotalRevenue() : java.math.BigDecimal.ZERO);
            summary.put("topPerformer", result.getSummary() != null ? result.getSummary().getTopPerformer() : null);
            data.put("summary", summary);

            base.setStatus(HttpStatus.OK.value());
            base.setData(data);
            base.setMessage("Booking chart computed");
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage("Invalid request: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(DELETE_BOOKING)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<BookingResponseDTO>> deleteBooking(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<BookingResponseDTO>();

        try {
            // If customer, ensure ownership before deleting
            String role = CurrentUser.getRole();
            if (role != null && role.contains("ROLE_CUSTOMER")) {
                String email = CurrentUser.getEmail();
                BookingResponseDTO existing = bookingRestService.getBooking(id);
                if (existing == null) {
                    baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                    baseResponseDTO.setMessage("Booking Tidak Ditemukan");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
                }
                if (email == null || !email.equalsIgnoreCase(existing.getContactEmail())) {
                    baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                    baseResponseDTO.setMessage("Access denied");
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
                }
            }

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

    // Internal callback from Bill service after successful payment.
    // This endpoint is called via POST /api/booking/payment/confirm with ConfirmPaymentRequestDTO.
    // Updates booking status to Paid (2).
    @PostMapping(BASE_URL + "/payment/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestBody ConfirmPaymentRequestDTO request) {
        logger.info("=== Received Payment Confirmation Request ===");
        logger.info("Service Reference ID: {}", request != null ? request.getServiceReferenceId() : "null");
        logger.info("Customer ID: {}", request != null ? request.getCustomerId() : "null");

        try {
            logger.info("Calling bookingRestService.confirmPayment...");
            BookingResponseDTO booking = bookingRestService.confirmPayment(request);
            
            logger.info("confirmPayment returned: {}", booking != null ? "BookingResponseDTO" : "null");
            if (booking != null) {
                logger.info("Booking ID: {}, New Status: {}", booking.getId(), booking.getStatus());
                logger.info("=== Payment Confirmation Success ===");
            }
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logger.error("=== Payment Confirmation Error ===");
            logger.error("Error Type: {}", ex.getClass().getSimpleName());
            logger.error("Error Message: {}", ex.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    private void adjustBookingStatuses(List<BookingResponseDTO> bookings, List<Bill> bills) {
        // Create a map of serviceReferenceId to bill status for quick lookup
        Map<String, Bill.BillStatus> billStatusMap = bills.stream()
                .collect(Collectors.toMap(Bill::getServiceReferenceId, Bill::getStatus));

        // Adjust booking status based on bill status
        for (BookingResponseDTO booking : bookings) {
            Bill.BillStatus billStatus = billStatusMap.get(booking.getId());
            if (billStatus == Bill.BillStatus.PAID) {
                booking.setStatus(2); // Paid
            }
            // If bill is UNPAID or no bill found, keep the current status (1 = Unpaid)
        }
    }
}

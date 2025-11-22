package apap.ti._5.flight_2306211660_be.restcontroller.flight;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.AddFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.flight.UpdateFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.flight.FlightRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Date;
import java.util.List;
import apap.ti._5.flight_2306211660_be.config.security.CurrentUser;

@RestController
@RequestMapping("/api")
public class FlightRestController {
    @Autowired
    private FlightRestService flightRestService;

    public static final String BASE_URL = "/flight";
    public static final String ALL_FLIGHTS = BASE_URL + "/all";
    public static final String ACTIVE_FLIGHTS_TODAY = BASE_URL + "/active/today";
    public static final String VIEW_FLIGHT = BASE_URL + "/{id}";
    public static final String CREATE_FLIGHT = BASE_URL + "/create";
    public static final String UPDATE_FLIGHT = BASE_URL + "/update";
    public static final String DELETE_FLIGHT = BASE_URL + "/delete/{id}";

    @GetMapping(ALL_FLIGHTS)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
        public ResponseEntity<BaseResponseDTO<List<FlightResponseDTO>>> getAllFlights(
            @RequestParam(required = false) String originAirportCode,
            @RequestParam(required = false) String destinationAirportCode,
            @RequestParam(required = false) String airlineId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean includeDeleted,
            @RequestParam(required = false) String search) {
        var baseResponseDTO = new BaseResponseDTO<List<FlightResponseDTO>>();

        try {
            // Only SUPERADMIN and FLIGHT_AIRLINE may request includeDeleted=true
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

            List<FlightResponseDTO> flights = flightRestService.getAllFlightsWithFilters(
                originAirportCode, destinationAirportCode, airlineId, status, includeDeleted, search);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(flights);
            baseResponseDTO.setMessage("Data Flight Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(ACTIVE_FLIGHTS_TODAY)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<Long>> getActiveFlightsToday() {
        var base = new BaseResponseDTO<Long>();
        try {
            long total = flightRestService.getActiveFlightsTodayCount();
            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setMessage("Active flights for today retrieved");
            base.setData(total);
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({BASE_URL + "/reminder", "/flights/reminder"})
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightReminderResponseDTO>>> getFlightReminders(
            @RequestParam(required = false) String interval,
            @RequestParam(required = false, name = "CustomerId") String customerIdStr) {
        var base = new BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightReminderResponseDTO>>();

        try {
            // Parse interval, allow formats like '3h' or '3'
            Integer hours = null;
            if (interval != null && !interval.isBlank()) {
                String s = interval.trim();
                if (s.endsWith("h") || s.endsWith("H")) s = s.substring(0, s.length() - 1);
                try {
                    hours = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    hours = 3;
                }
            }
            if (hours == null || hours <= 0) hours = 3;

            // Handle customerId and role restrictions: if role CUSTOMER, force customerUserId to current user's id
            String customerUserId = null;
            String role = CurrentUser.getRole();
            String currentUserId = CurrentUser.getUserId();
            if (role != null && role.contains("ROLE_CUSTOMER")) {
                // enforce that customer only sees their own flights
                if (currentUserId == null) {
                    base.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                    base.setMessage("Access denied");
                    base.setTimestamp(new java.util.Date());
                    return new ResponseEntity<>(base, org.springframework.http.HttpStatus.FORBIDDEN);
                }
                customerUserId = currentUserId;
            } else {
                if (customerIdStr != null && !customerIdStr.isBlank()) {
                    customerUserId = customerIdStr.trim();
                }
            }

            java.util.List<apap.ti._5.flight_2306211660_be.restdto.response.flight.FlightReminderResponseDTO> data =
                    flightRestService.getFlightReminders(hours, customerUserId);

            if (data == null || data.isEmpty()) {
                base.setStatus(org.springframework.http.HttpStatus.OK.value());
                base.setData(java.util.List.of());
                base.setMessage("No upcoming flights found.");
                base.setTimestamp(new java.util.Date());
                return ResponseEntity.ok(base);
            }

            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setData(data);
            base.setMessage("Upcoming flights retrieved");
            base.setTimestamp(new java.util.Date());
            return ResponseEntity.ok(base);

        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(VIEW_FLIGHT)
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<FlightResponseDTO>> getFlight(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<FlightResponseDTO>();

        FlightResponseDTO flight = flightRestService.getFlightDetail(id);

        if (flight == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Flight Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(flight);
        baseResponseDTO.setMessage("Data Flight Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_FLIGHT)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<FlightResponseDTO>> createFlight(
            @Valid @RequestBody AddFlightRequestDTO addFlightRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<FlightResponseDTO>();

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
            FlightResponseDTO flight = flightRestService.createFlight(addFlightRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(flight);
            baseResponseDTO.setMessage("Data Flight Berhasil Dibuat");
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

    @PutMapping(UPDATE_FLIGHT)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<FlightResponseDTO>> updateFlight(
            @Valid @RequestBody UpdateFlightRequestDTO updateFlightRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<FlightResponseDTO>();

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
            FlightResponseDTO flight = flightRestService.updateFlight(updateFlightRequestDTO);

            if (flight == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Flight Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(flight);
            baseResponseDTO.setMessage("Data Flight Berhasil Diupdate");
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

    @PostMapping(DELETE_FLIGHT)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<FlightResponseDTO>> deleteFlight(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<FlightResponseDTO>();

        try {
            FlightResponseDTO flight = flightRestService.deleteFlight(id);

            if (flight == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Flight Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(flight);
            baseResponseDTO.setMessage("Data Flight Berhasil Dihapus");
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
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

@RestController
@RequestMapping("/api")
public class FlightRestController {
    @Autowired
    private FlightRestService flightRestService;

    public static final String BASE_URL = "/flight";
    public static final String ALL_FLIGHTS = BASE_URL + "/all";
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
            @RequestParam(required = false) Boolean includeDeleted) {
        var baseResponseDTO = new BaseResponseDTO<List<FlightResponseDTO>>();

        try {
            List<FlightResponseDTO> flights = flightRestService.getAllFlightsWithFilters(
                originAirportCode, destinationAirportCode, airlineId, status, includeDeleted);

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
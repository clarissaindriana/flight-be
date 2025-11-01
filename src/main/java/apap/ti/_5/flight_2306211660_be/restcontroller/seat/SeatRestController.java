package apap.ti._5.flight_2306211660_be.restcontroller.seat;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.AddSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.seat.UpdateSeatRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.seat.SeatRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SeatRestController {
    @Autowired
    private SeatRestService seatRestService;

    public static final String BASE_URL = "/seat";
    public static final String VIEW_SEAT = BASE_URL + "/{id}";
    public static final String CREATE_SEAT = BASE_URL + "/create";
    public static final String UPDATE_SEAT = BASE_URL + "/update";
    public static final String DELETE_SEAT = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<SeatResponseDTO>>> getAllSeats(
            @RequestParam(required = false) Integer classFlightId,
            @RequestParam(required = false) String flightId) {
        var baseResponseDTO = new BaseResponseDTO<List<SeatResponseDTO>>();

        List<SeatResponseDTO> seats;

        if (flightId != null) {
            seats = seatRestService.getSeatsByFlight(flightId);
        } else if (classFlightId != null) {
            seats = seatRestService.getSeatsByClassFlight(classFlightId);
        } else {
            seats = seatRestService.getAllSeats();
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(seats);
        baseResponseDTO.setMessage("Data Seat Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_SEAT)
    public ResponseEntity<BaseResponseDTO<SeatResponseDTO>> getSeat(@PathVariable Integer id) {
        var baseResponseDTO = new BaseResponseDTO<SeatResponseDTO>();

        SeatResponseDTO seat = seatRestService.getSeat(id);

        if (seat == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Seat Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(seat);
        baseResponseDTO.setMessage("Data Seat Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_SEAT)
    public ResponseEntity<BaseResponseDTO<SeatResponseDTO>> createSeat(
            @Valid @RequestBody AddSeatRequestDTO addSeatRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<SeatResponseDTO>();

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
            SeatResponseDTO seat = seatRestService.createSeat(addSeatRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(seat);
            baseResponseDTO.setMessage("Data Seat Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_SEAT)
    public ResponseEntity<BaseResponseDTO<SeatResponseDTO>> updateSeat(
            @Valid @RequestBody UpdateSeatRequestDTO updateSeatRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<SeatResponseDTO>();

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
            SeatResponseDTO seat = seatRestService.updateSeat(updateSeatRequestDTO);

            if (seat == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Seat Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(seat);
            baseResponseDTO.setMessage("Data Seat Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(DELETE_SEAT)
    public ResponseEntity<BaseResponseDTO<SeatResponseDTO>> deleteSeat(
            @PathVariable Integer id) {
        var baseResponseDTO = new BaseResponseDTO<SeatResponseDTO>();

        try {
            SeatResponseDTO seat = seatRestService.deleteSeat(id);

            if (seat == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Seat Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(seat);
            baseResponseDTO.setMessage("Data Seat Berhasil Dihapus");
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

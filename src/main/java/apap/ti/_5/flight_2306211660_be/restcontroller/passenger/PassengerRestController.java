package apap.ti._5.flight_2306211660_be.restcontroller.passenger;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.passenger.PassengerRestService;
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
public class PassengerRestController {
    @Autowired
    private PassengerRestService passengerRestService;

    public static final String BASE_URL = "/passenger";
    public static final String VIEW_PASSENGER = BASE_URL + "/{id}";
    public static final String CREATE_PASSENGER = BASE_URL + "/create";
    public static final String UPDATE_PASSENGER = BASE_URL + "/update";
    public static final String DELETE_PASSENGER = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<PassengerResponseDTO>>> getAllPassengers() {
        var baseResponseDTO = new BaseResponseDTO<List<PassengerResponseDTO>>();

        List<PassengerResponseDTO> passengers = passengerRestService.getAllPassengers();

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(passengers);
        baseResponseDTO.setMessage("Data Passenger Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_PASSENGER)
    public ResponseEntity<BaseResponseDTO<PassengerResponseDTO>> getPassenger(@PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<PassengerResponseDTO>();

        PassengerResponseDTO passenger = passengerRestService.getPassenger(id);

        if (passenger == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Passenger Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(passenger);
        baseResponseDTO.setMessage("Data Passenger Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_PASSENGER)
    public ResponseEntity<BaseResponseDTO<PassengerResponseDTO>> createPassenger(
            @Valid @RequestBody AddPassengerRequestDTO addPassengerRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PassengerResponseDTO>();

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
            PassengerResponseDTO passenger = passengerRestService.createPassenger(addPassengerRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(passenger);
            baseResponseDTO.setMessage("Data Passenger Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_PASSENGER)
    public ResponseEntity<BaseResponseDTO<PassengerResponseDTO>> updatePassenger(
            @Valid @RequestBody UpdatePassengerRequestDTO updatePassengerRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PassengerResponseDTO>();

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
            PassengerResponseDTO passenger = passengerRestService.updatePassenger(updatePassengerRequestDTO);

            if (passenger == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Passenger Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(passenger);
            baseResponseDTO.setMessage("Data Passenger Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(DELETE_PASSENGER)
    public ResponseEntity<BaseResponseDTO<PassengerResponseDTO>> deletePassenger(
            @PathVariable UUID id) {
        var baseResponseDTO = new BaseResponseDTO<PassengerResponseDTO>();

        try {
            PassengerResponseDTO passenger = passengerRestService.deletePassenger(id);

            if (passenger == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Passenger Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(passenger);
            baseResponseDTO.setMessage("Data Passenger Berhasil Dihapus");
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

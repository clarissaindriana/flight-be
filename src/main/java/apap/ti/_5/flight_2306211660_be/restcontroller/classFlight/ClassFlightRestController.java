package apap.ti._5.flight_2306211660_be.restcontroller.classFlight;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.UpdateClassFlightRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.classFlight.ClassFlightRestService;
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
public class ClassFlightRestController {
    @Autowired
    private ClassFlightRestService classFlightRestService;

    public static final String BASE_URL = "/classFlight";
    public static final String VIEW_CLASS_FLIGHT = BASE_URL + "/{id}";
    public static final String CREATE_CLASS_FLIGHT = BASE_URL + "/create";
    public static final String UPDATE_CLASS_FLIGHT = BASE_URL + "/update";
    public static final String DELETE_CLASS_FLIGHT = BASE_URL + "/delete/{id}";

    @GetMapping(BASE_URL)
    public ResponseEntity<BaseResponseDTO<List<ClassFlightResponseDTO>>> getAllClassFlights(
            @RequestParam(required = false) String flightId) {
        var baseResponseDTO = new BaseResponseDTO<List<ClassFlightResponseDTO>>();

        List<ClassFlightResponseDTO> classFlights;

        if (flightId != null) {
            classFlights = classFlightRestService.getClassFlightsByFlight(flightId);
        } else {
            classFlights = classFlightRestService.getAllClassFlights();
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(classFlights);
        baseResponseDTO.setMessage("Data ClassFlight Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_CLASS_FLIGHT)
    public ResponseEntity<BaseResponseDTO<ClassFlightResponseDTO>> getClassFlight(@PathVariable Integer id) {
        var baseResponseDTO = new BaseResponseDTO<ClassFlightResponseDTO>();

        ClassFlightResponseDTO classFlight = classFlightRestService.getClassFlight(id);

        if (classFlight == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("ClassFlight Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(classFlight);
        baseResponseDTO.setMessage("Data ClassFlight Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_CLASS_FLIGHT)
    public ResponseEntity<BaseResponseDTO<ClassFlightResponseDTO>> createClassFlight(
            @Valid @RequestBody AddClassFlightRequestDTO addClassFlightRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<ClassFlightResponseDTO>();

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
            ClassFlightResponseDTO classFlight = classFlightRestService.createClassFlight(addClassFlightRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(classFlight);
            baseResponseDTO.setMessage("Data ClassFlight Berhasil Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(UPDATE_CLASS_FLIGHT)
    public ResponseEntity<BaseResponseDTO<ClassFlightResponseDTO>> updateClassFlight(
            @Valid @RequestBody UpdateClassFlightRequestDTO updateClassFlightRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<ClassFlightResponseDTO>();

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
            ClassFlightResponseDTO classFlight = classFlightRestService.updateClassFlight(updateClassFlightRequestDTO);

            if (classFlight == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("ClassFlight Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(classFlight);
            baseResponseDTO.setMessage("Data ClassFlight Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(DELETE_CLASS_FLIGHT)
    public ResponseEntity<BaseResponseDTO<ClassFlightResponseDTO>> deleteClassFlight(
            @PathVariable Integer id) {
        var baseResponseDTO = new BaseResponseDTO<ClassFlightResponseDTO>();

        try {
            ClassFlightResponseDTO classFlight = classFlightRestService.deleteClassFlight(id);

            if (classFlight == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("ClassFlight Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(classFlight);
            baseResponseDTO.setMessage("Data ClassFlight Berhasil Dihapus");
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

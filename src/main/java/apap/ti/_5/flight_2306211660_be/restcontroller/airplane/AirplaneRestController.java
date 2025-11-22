package apap.ti._5.flight_2306211660_be.restcontroller.airplane;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.AddAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airplane.UpdateAirplaneRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airplane.AirplaneResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.airplane.AirplaneRestService;
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
public class AirplaneRestController {
    @Autowired
    private AirplaneRestService airplaneRestService;

    public static final String BASE_URL = "/airplane";
    public static final String VIEW_ALL_AIRPLANES = BASE_URL + "/all";
    public static final String VIEW_AIRPLANE = BASE_URL + "/{id}";
    public static final String CREATE_AIRPLANE = BASE_URL + "/create";
    public static final String UPDATE_AIRPLANE = BASE_URL + "/update";
    public static final String DELETE_AIRPLANE = BASE_URL + "/{id}/delete";
    public static final String ACTIVATE_AIRPLANE = BASE_URL + "/{id}/activate";

    @GetMapping({VIEW_ALL_AIRPLANES, "/airplanes/all"})
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<List<AirplaneResponseDTO>>> getAllAirplanes(
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String airlineId,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer manufactureYear) {

        var baseResponseDTO = new BaseResponseDTO<List<AirplaneResponseDTO>>();

        try {
            List<AirplaneResponseDTO> airplanes = airplaneRestService.getAllAirplanes();

            // Filter by isDeleted if provided
            if (isDeleted != null) {
                airplanes = airplanes.stream()
                        .filter(airplane -> airplane.isDeleted() == isDeleted)
                        .toList();
            }

            // Filter by search (id, model, or airlineId) - case-insensitive
            if (search != null && !search.trim().isEmpty()) {
                String q = search.toLowerCase().trim();
                airplanes = airplanes.stream()
                        .filter(airplane ->
                                (airplane.getId() != null && airplane.getId().toLowerCase().contains(q)) ||
                                (airplane.getModel() != null && airplane.getModel().toLowerCase().contains(q)) ||
                                (airplane.getAirlineId() != null && airplane.getAirlineId().toLowerCase().contains(q))
                        )
                        .toList();
            }

            // Filter by airlineId
            if (airlineId != null && !airlineId.trim().isEmpty()) {
                airplanes = airplanes.stream()
                        .filter(airplane -> airplane.getAirlineId().equals(airlineId))
                        .toList();
            }

            // Filter by model
            if (model != null && !model.trim().isEmpty()) {
                airplanes = airplanes.stream()
                        .filter(airplane -> airplane.getModel().toLowerCase().contains(model.toLowerCase()))
                        .toList();
            }

            // Filter by manufacture year
            if (manufactureYear != null) {
                airplanes = airplanes.stream()
                        .filter(airplane -> airplane.getManufactureYear().equals(manufactureYear))
                        .toList();
            }

            // Sort by registration number ascending
            airplanes = airplanes.stream()
                    .sorted((a1, a2) -> a1.getId().compareTo(a2.getId()))
                    .toList();

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airplanes);
            baseResponseDTO.setMessage("Data Airplane Berhasil Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(VIEW_AIRPLANE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<AirplaneResponseDTO>> getAirplane(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<AirplaneResponseDTO>();

        AirplaneResponseDTO airplane = airplaneRestService.getAirplane(id);

        if (airplane == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Airplane Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(airplane);
        baseResponseDTO.setMessage("Data Airplane Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_AIRPLANE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<AirplaneResponseDTO>> createAirplane(
            @Valid @RequestBody AddAirplaneRequestDTO addAirplaneRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirplaneResponseDTO>();

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
            AirplaneResponseDTO airplane = airplaneRestService.createAirplane(addAirplaneRequestDTO);

            baseResponseDTO.setStatus(HttpStatus.CREATED.value());
            baseResponseDTO.setData(airplane);
            baseResponseDTO.setMessage("Data Airplane Berhasil Dibuat");
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

    @PutMapping(UPDATE_AIRPLANE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<AirplaneResponseDTO>> updateAirplane(
            @Valid @RequestBody UpdateAirplaneRequestDTO updateAirplaneRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirplaneResponseDTO>();

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
            AirplaneResponseDTO airplane = airplaneRestService.updateAirplane(updateAirplaneRequestDTO);

            if (airplane == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airplane Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airplane);
            baseResponseDTO.setMessage("Data Airplane Berhasil Diupdate");
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

    @PostMapping(DELETE_AIRPLANE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<AirplaneResponseDTO>> deleteAirplane(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<AirplaneResponseDTO>();

        try {
            AirplaneResponseDTO airplane = airplaneRestService.deleteAirplane(id);

            if (airplane == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airplane Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airplane);
            baseResponseDTO.setMessage("Data Airplane Berhasil Dihapus");
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

    @PostMapping(ACTIVATE_AIRPLANE)
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<AirplaneResponseDTO>> activateAirplane(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<AirplaneResponseDTO>();

        try {
            AirplaneResponseDTO airplane = airplaneRestService.activateAirplane(id);

            if (airplane == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airplane Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airplane);
            baseResponseDTO.setMessage("Data Airplane Berhasil Diaktifkan");
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

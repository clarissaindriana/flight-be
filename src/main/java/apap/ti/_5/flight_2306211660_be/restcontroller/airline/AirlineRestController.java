package apap.ti._5.flight_2306211660_be.restcontroller.airline;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.AddAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airline.UpdateAirlineRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.airline.AirlineRestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class AirlineRestController {
    @Autowired
    private AirlineRestService airlineRestService;

    public static final String BASE_URL = "/airline";
    public static final String VIEW_ALL_AIRLINES = BASE_URL + "/all";
    public static final String VIEW_AIRLINE = BASE_URL + "/{id}";
    public static final String CREATE_AIRLINE = BASE_URL + "/create";
    public static final String UPDATE_AIRLINE = BASE_URL + "/update";
    public static final String DELETE_AIRLINE = BASE_URL + "/delete/{id}";

    @GetMapping(VIEW_ALL_AIRLINES)
    public ResponseEntity<BaseResponseDTO<List<AirlineResponseDTO>>> getAllAirlines(
            @RequestParam(required = false) String search) {
        var baseResponseDTO = new BaseResponseDTO<List<AirlineResponseDTO>>();

        List<AirlineResponseDTO> listAirline;

        if(search != null) {
            listAirline = airlineRestService.searchAirlinesByName(search);
        }else{
            listAirline = airlineRestService.getAllAirlines();
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listAirline);
        baseResponseDTO.setMessage("Data Airline Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_AIRLINE)
    public ResponseEntity<BaseResponseDTO<AirlineResponseDTO>> getAirline(@PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<AirlineResponseDTO>();

        AirlineResponseDTO airline = airlineRestService.getAirline(id);

        if (airline == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Airline Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(airline);
        baseResponseDTO.setMessage("Data Airline Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(BASE_URL + "/total")
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<Long>> getTotalAirlines() {
        var base = new BaseResponseDTO<Long>();
        try {
            long total = airlineRestService.getTotalAirlines();
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Total airlines retrieved");
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

    @PostMapping(CREATE_AIRLINE)
    public ResponseEntity<BaseResponseDTO<AirlineResponseDTO>> createAirline(
            @Valid @RequestBody AddAirlineRequestDTO addAirlineRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirlineResponseDTO>();

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


        AirlineResponseDTO airline = airlineRestService.createAirline(addAirlineRequestDTO);

        if (airline == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Airline Gagal Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData(airline);
        baseResponseDTO.setMessage("Data Airline Berhasil Dibuat");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping(UPDATE_AIRLINE)
    public ResponseEntity<BaseResponseDTO<AirlineResponseDTO>> updateAirline(
            @Valid @RequestBody UpdateAirlineRequestDTO updateAirlineRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirlineResponseDTO>();

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
            AirlineResponseDTO airline = airlineRestService.updateAirline(updateAirlineRequestDTO);

            if (airline == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airline Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airline);
            baseResponseDTO.setMessage("Data Airline Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_AIRLINE)
    public ResponseEntity<BaseResponseDTO<AirlineResponseDTO>> deleteAirline(
            @PathVariable String id) {
        var baseResponseDTO = new BaseResponseDTO<AirlineResponseDTO>();

        try {
            AirlineResponseDTO airline = airlineRestService.deleteAirline(id);

            if (airline == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airline Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airline);
            baseResponseDTO.setMessage("Data Airline Berhasil Dihapus");
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

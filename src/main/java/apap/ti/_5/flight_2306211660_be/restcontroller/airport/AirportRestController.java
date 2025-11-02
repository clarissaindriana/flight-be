package apap.ti._5.flight_2306211660_be.restcontroller.airport;

import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.AddAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.airport.UpdateAirportRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.airport.AirportResponseDTO;

import apap.ti._5.flight_2306211660_be.restservice.airport.AirportRestService;
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
public class AirportRestController {
    @Autowired
    private AirportRestService airportRestService;

    public static final String BASE_URL = "/airport";
    public static final String VIEW_ALL_AIRPORTS = BASE_URL + "/all";
    public static final String VIEW_AIRPORT = BASE_URL + "/{iataCode}";
    public static final String CREATE_AIRPORT = BASE_URL + "/create";
    public static final String UPDATE_AIRPORT = BASE_URL + "/update";
    public static final String DELETE_AIRPORT = BASE_URL + "/delete/{iataCode}";

    @GetMapping(VIEW_ALL_AIRPORTS)
    public ResponseEntity<BaseResponseDTO<List<AirportResponseDTO>>> getAllAirports(
            @RequestParam(required = false) String search) {
        var baseResponseDTO = new BaseResponseDTO<List<AirportResponseDTO>>();

        List<AirportResponseDTO> listAirport;

        if(search != null) {
            listAirport = airportRestService.searchAirportsByName(search);
        }else{
            listAirport = airportRestService.getAllAirports();
        }

        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(listAirport);
        baseResponseDTO.setMessage("Data Airport Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @GetMapping(VIEW_AIRPORT)
    public ResponseEntity<BaseResponseDTO<AirportResponseDTO>> getAirport(@PathVariable String iataCode) {
        var baseResponseDTO = new BaseResponseDTO<AirportResponseDTO>();

        AirportResponseDTO airport = airportRestService.getAirport(iataCode);

        if (airport == null) {
            baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
            baseResponseDTO.setMessage("Airport Tidak Ditemukan");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
        }
        baseResponseDTO.setStatus(HttpStatus.OK.value());
        baseResponseDTO.setData(airport);
        baseResponseDTO.setMessage("Data Airport Berhasil Ditemukan");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);
    }

    @PostMapping(CREATE_AIRPORT)
    public ResponseEntity<BaseResponseDTO<AirportResponseDTO>> createAirport(
            @Valid @RequestBody AddAirportRequestDTO addAirportRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirportResponseDTO>();

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


        AirportResponseDTO airport = airportRestService.createAirport(addAirportRequestDTO);

        if (airport == null) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Airport Gagal Dibuat");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        baseResponseDTO.setStatus(HttpStatus.CREATED.value());
        baseResponseDTO.setData(airport);
        baseResponseDTO.setMessage("Data Airport Berhasil Dibuat");
        baseResponseDTO.setTimestamp(new Date());
        return new ResponseEntity<>(baseResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping(UPDATE_AIRPORT)
    public ResponseEntity<BaseResponseDTO<AirportResponseDTO>> updateAirport(
            @Valid @RequestBody UpdateAirportRequestDTO updateAirportRequestDTO,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<AirportResponseDTO>();

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
            AirportResponseDTO airport = airportRestService.updateAirport(updateAirportRequestDTO);

            if (airport == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airport Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airport);
            baseResponseDTO.setMessage("Data Airport Berhasil Diupdate");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception ex) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada server: " + ex.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(DELETE_AIRPORT)
    public ResponseEntity<BaseResponseDTO<AirportResponseDTO>> deleteAirport(
            @PathVariable String iataCode) {
        var baseResponseDTO = new BaseResponseDTO<AirportResponseDTO>();

        try {
            AirportResponseDTO airport = airportRestService.deleteAirport(iataCode);

            if (airport == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("Airport Tidak Ditemukan");
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(airport);
            baseResponseDTO.setMessage("Data Airport Berhasil Dihapus");
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

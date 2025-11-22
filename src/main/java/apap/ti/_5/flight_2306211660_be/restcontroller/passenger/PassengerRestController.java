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
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Date;
import java.util.List;
import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.model.Passenger;
import apap.ti._5.flight_2306211660_be.repository.PassengerRepository;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PassengerRestController {
    @Autowired
    private PassengerRestService passengerRestService;

    @Autowired
    private ProfileClient profileClient;

    @Autowired
    private PassengerRepository passengerRepository;

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

    // --- User management proxy endpoints (profile microservice) ---
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>>> getAllUsers() {
        var base = new BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>>();
        try {
            var wrap = profileClient.getAllUsers();
            java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser> data = (wrap != null && wrap.getData() != null) ? wrap.getData() : java.util.List.of();
            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setData(data);
            base.setMessage("Users retrieved");
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>> getUserById(@PathVariable String id) {
        var base = new BaseResponseDTO<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>();
        try {
            var wrap = profileClient.getUserById(id);
            if (wrap == null || wrap.getData() == null) {
                base.setStatus(org.springframework.http.HttpStatus.NOT_FOUND.value());
                base.setMessage("User not found");
                base.setTimestamp(new Date());
                return new ResponseEntity<>(base, org.springframework.http.HttpStatus.NOT_FOUND);
            }
            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setData(wrap.getData());
            base.setMessage("User retrieved");
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>> updateUserById(@PathVariable String id, @RequestBody Object payload) {
        var base = new BaseResponseDTO<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>();
        try {
            var wrap = profileClient.updateUser(id, payload);
            if (wrap == null || wrap.getData() == null) {
                base.setStatus(org.springframework.http.HttpStatus.BAD_REQUEST.value());
                base.setMessage("Failed to update user");
                base.setTimestamp(new Date());
                return new ResponseEntity<>(base, org.springframework.http.HttpStatus.BAD_REQUEST);
            }
            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setData(wrap.getData());
            base.setMessage("User updated");
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/customers")
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE')")
    public ResponseEntity<BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>>> getCustomersAndSync() {
        var base = new BaseResponseDTO<java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser>>();
        try {
            var wrap = profileClient.getCustomers();
            java.util.List<apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileUser> users = (wrap != null && wrap.getData() != null) ? wrap.getData() : java.util.List.of();

            // Sync to local Passenger table: map profile user id -> idPassport
            for (var u : users) {
                if (u.getId() == null) continue;
                if (!passengerRepository.existsByIdPassport(u.getId())) {
                    Passenger p = Passenger.builder()
                            .id(java.util.UUID.randomUUID())
                            .fullName(u.getName() != null ? u.getName() : u.getUsername())
                            .birthDate(LocalDate.of(1970,1,1))
                            .gender(1)
                            .idPassport(u.getId())
                            .build();
                    passengerRepository.save(p);
                }
            }

            base.setStatus(org.springframework.http.HttpStatus.OK.value());
            base.setData(users);
            base.setMessage("Customers retrieved and synced");
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

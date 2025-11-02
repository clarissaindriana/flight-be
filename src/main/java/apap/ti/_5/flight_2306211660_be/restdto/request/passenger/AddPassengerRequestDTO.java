package apap.ti._5.flight_2306211660_be.restdto.request.passenger;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPassengerRequestDTO {

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotNull(message = "Birth date cannot be null")
    private LocalDate birthDate;

    @NotNull(message = "Gender cannot be null")
    private Integer gender;

    @NotBlank(message = "ID passport cannot be blank")
    private String idPassport;
}

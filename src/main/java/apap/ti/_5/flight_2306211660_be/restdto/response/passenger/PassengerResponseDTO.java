package apap.ti._5.flight_2306211660_be.restdto.response.passenger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerResponseDTO {

    private UUID id;
    private String fullName;
    private LocalDate birthDate;
    private Integer gender;
    private String idPassport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

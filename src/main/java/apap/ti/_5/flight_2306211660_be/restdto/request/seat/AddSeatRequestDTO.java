package apap.ti._5.flight_2306211660_be.restdto.request.seat;

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
public class AddSeatRequestDTO {

    @NotNull(message = "Class flight ID cannot be null")
    private Integer classFlightId;

    @NotBlank(message = "Seat code cannot be blank")
    private String seatCode;
}

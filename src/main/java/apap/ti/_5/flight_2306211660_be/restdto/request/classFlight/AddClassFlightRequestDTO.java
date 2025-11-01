package apap.ti._5.flight_2306211660_be.restdto.request.classFlight;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddClassFlightRequestDTO {

    @NotBlank(message = "Class type cannot be blank")
    private String classType;

    @NotNull(message = "Seat capacity cannot be null")
    @Positive(message = "Seat capacity must be positive")
    private Integer seatCapacity;

    @NotNull(message = "Price cannot be null")
    private BigDecimal price;

    @NotBlank(message = "Flight ID cannot be blank")
    private String flightId;
}

package apap.ti._5.flight_2306211660_be.restdto.request.classFlight;

import java.math.BigDecimal;

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
public class UpdateClassFlightRequestDTO {

    @NotNull(message = "ID cannot be null")
    private Integer id;

    @NotNull(message = "Seat capacity cannot be null")
    @Positive(message = "Seat capacity must be positive")
    private Integer seatCapacity;

    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
}

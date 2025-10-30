package apap.ti._5.flight_2306211660_be.restdto.request.airplane;

import jakarta.validation.constraints.Min;
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
public class AddAirplaneRequestDTO {
    @NotBlank(message = "Airline ID cannot be blank")
    private String airlineId;

    @NotBlank(message = "Model cannot be blank")
    private String model;

    @NotNull(message = "Seat capacity cannot be null")
    @Min(value = 1, message = "Seat capacity must be greater than 0")
    private Integer seatCapacity;

    @NotNull(message = "Manufacture year cannot be null")
    @Min(value = 1900, message = "Manufacture year must be at least 1900")
    private Integer manufactureYear;
}

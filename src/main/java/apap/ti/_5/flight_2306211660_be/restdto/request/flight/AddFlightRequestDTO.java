package apap.ti._5.flight_2306211660_be.restdto.request.flight;

import java.time.LocalDateTime;
import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.classFlight.AddClassFlightRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class AddFlightRequestDTO {

    @NotBlank(message = "Airline ID cannot be blank")
    private String airlineId;

    @NotBlank(message = "Airplane ID cannot be blank")
    private String airplaneId;

    @NotBlank(message = "Origin airport code cannot be blank")
    private String originAirportCode;

    @NotBlank(message = "Destination airport code cannot be blank")
    private String destinationAirportCode;

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;

    @NotBlank(message = "Terminal cannot be blank")
    private String terminal;

    @NotBlank(message = "Gate cannot be blank")
    private String gate;

    @NotNull(message = "Baggage allowance cannot be null")
    @Positive(message = "Baggage allowance must be positive")
    private Integer baggageAllowance;

    private String facilities;

    @NotEmpty(message = "Classes cannot be empty")
    private List<AddClassFlightRequestDTO> classes;
}
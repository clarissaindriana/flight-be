package apap.ti._5.flight_2306211660_be.restdto.request.booking;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
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
public class AddBookingRequestDTO {

    @NotBlank(message = "Flight ID cannot be blank")
    private String flightId;

    @NotNull(message = "Class flight ID cannot be null")
    private Integer classFlightId;

    @NotBlank(message = "Contact email cannot be blank")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Contact phone cannot be blank")
    private String contactPhone;

    @NotNull(message = "Passenger count cannot be null")
    @Positive(message = "Passenger count must be positive")
    @Max(value = 10, message = "Passenger count cannot exceed 10")
    private Integer passengerCount;

    @NotEmpty(message = "Passengers cannot be empty")
    private List<AddPassengerRequestDTO> passengers;

    private List<Integer> seatIds; // Optional: specific seat assignments
}

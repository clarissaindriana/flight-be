package apap.ti._5.flight_2306211660_be.restdto.request.bookingPassenger;

import java.util.UUID;

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
public class AddBookingPassengerRequestDTO {

    @NotBlank(message = "Booking ID cannot be blank")
    private String bookingId;

    @NotNull(message = "Passenger ID cannot be null")
    private UUID passengerId;
}

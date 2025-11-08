package apap.ti._5.flight_2306211660_be.restdto.request.booking;

import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.request.passenger.AddPassengerRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.passenger.UpdatePassengerRequestDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequestDTO {

    @NotBlank(message = "Booking ID cannot be blank")
    private String id;

    @NotBlank(message = "Contact email cannot be blank")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Contact phone cannot be blank")
    private String contactPhone;

    // Existing passengers to update and keep in this booking (by ID)
    private List<UpdatePassengerRequestDTO> passengers;

    // New passengers to add into this booking (optional)
    private List<AddPassengerRequestDTO> newPassengers;

    // Optional: specific seat assignments (must equal resulting passenger count if provided)
    private List<Integer> seatIds;
}

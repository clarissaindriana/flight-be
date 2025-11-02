package apap.ti._5.flight_2306211660_be.restdto.response.bookingPassanger;

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
public class BookingPassengerResponseDTO {

    private String bookingId;
    private UUID passengerId;
    private LocalDateTime createdAt;
}

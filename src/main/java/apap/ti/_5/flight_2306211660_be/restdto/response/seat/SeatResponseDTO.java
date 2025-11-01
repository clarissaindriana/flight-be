package apap.ti._5.flight_2306211660_be.restdto.response.seat;

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
public class SeatResponseDTO {

    private Integer id;
    private Integer classFlightId;
    private UUID passengerId;
    private String seatCode;
    private Boolean isBooked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

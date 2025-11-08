package apap.ti._5.flight_2306211660_be.restdto.response.booking;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerSeatAssignmentResponseDTO {
    private UUID passengerId;
    private Integer seatId;
    private String seatCode;
}
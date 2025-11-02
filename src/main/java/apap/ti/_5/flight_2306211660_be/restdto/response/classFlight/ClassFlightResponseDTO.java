package apap.ti._5.flight_2306211660_be.restdto.response.classFlight;

import java.math.BigDecimal;
import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.response.seat.SeatResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassFlightResponseDTO {

    private Integer id;
    private String flightId;
    private String classType;
    private Integer seatCapacity;
    private Integer availableSeats;
    private BigDecimal price;
    private List<SeatResponseDTO> seats; // For detailed view
}

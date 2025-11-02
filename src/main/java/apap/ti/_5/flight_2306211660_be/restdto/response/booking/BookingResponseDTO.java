package apap.ti._5.flight_2306211660_be.restdto.response.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.response.passenger.PassengerResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    private String id;
    private String flightId;
    private Integer classFlightId;
    private String contactEmail;
    private String contactPhone;
    private Integer passengerCount;
    private Integer status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<PassengerResponseDTO> passengers;
}

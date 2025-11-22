package apap.ti._5.flight_2306211660_be.restdto.response.booking;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingChartResponseDTO {
    private String flightId;
    private String airlineName;
    private String origin;
    private String destination;
    private Long totalBookings;
    private BigDecimal totalRevenue;
}

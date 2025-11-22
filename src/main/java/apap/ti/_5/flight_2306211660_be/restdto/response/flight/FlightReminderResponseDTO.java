package apap.ti._5.flight_2306211660_be.restdto.response.flight;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightReminderResponseDTO {
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private Long remainingTimeMinutes;
    private Integer status;
    private Long totalPaidBookings;
    private Long totalUnpaidBookings;
}

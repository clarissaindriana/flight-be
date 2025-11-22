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
    private String route;

    // Numeric identifier of class flight (kept for backward compatibility)
    private Integer classFlightId;

    // Human-readable class type (Economy, Business, First)
    private String classType;

    private String contactEmail;
    private String contactPhone;
    private Integer passengerCount;

    // 1=Unpaid, 2=Paid, 3=Cancelled, 4=Rescheduled
    private Integer status;

    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Soft delete flag
    private Boolean isDeleted;

    // Passengers in this booking
    private List<PassengerResponseDTO> passengers;

    // Seat assignments for each passenger in this booking (for detail display)
    private List<PassengerSeatAssignmentResponseDTO> seatAssignments;
}

package apap.ti._5.flight_2306211660_be.restdto.response.flight;

import java.time.LocalDateTime;
import java.util.List;

import apap.ti._5.flight_2306211660_be.restdto.response.classFlight.ClassFlightResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponseDTO {
    private String id;
    private String airlineId;
    private String airplaneId;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String terminal;
    private String gate;
    private int baggageAllowance;
    private String facilities;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private List<ClassFlightResponseDTO> classes;
}
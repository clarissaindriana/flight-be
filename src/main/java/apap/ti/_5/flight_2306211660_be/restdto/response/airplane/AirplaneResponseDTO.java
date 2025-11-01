package apap.ti._5.flight_2306211660_be.restdto.response.airplane;

import java.time.LocalDateTime;

import apap.ti._5.flight_2306211660_be.restdto.response.airline.AirlineResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirplaneResponseDTO {
    private String id;
    private String airlineId;
    // private AirlineResponseDTO airline;
    private String model;
    private Integer seatCapacity;
    private Integer manufactureYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted = false;
}

package apap.ti._5.flight_2306211660_be.restdto.response.airport;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponseDTO {

    private String iataCode;
    private String name;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

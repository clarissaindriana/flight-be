package apap.ti._5.flight_2306211660_be.restdto.request.airport;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAirportRequestDTO {

    @NotBlank(message = "IATA code cannot be blank")
    private String iataCode;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    private Double latitude;

    private Double longitude;

    private String timezone;
}

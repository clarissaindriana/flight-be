package apap.ti._5.flight_2306211660_be.restdto.request.airline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAirlineRequestDTO {
    @NotBlank(message = "ID cannot be blank")
    @Size(min = 2, max = 3, message = "ID must be 2-3 characters")
    private String id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Country cannot be blank")
    private String country;
}

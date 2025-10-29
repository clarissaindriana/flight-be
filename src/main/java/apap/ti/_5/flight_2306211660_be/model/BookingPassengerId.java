package apap.ti._5.flight_2306211660_be.model;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPassengerId implements Serializable {

    private String bookingId;
    private UUID passengerId;
}
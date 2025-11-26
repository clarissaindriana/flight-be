package apap.ti._5.flight_2306211660_be.restdto.request.bill;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequestDTO {
    private UUID billId;
    private String customerId;
}

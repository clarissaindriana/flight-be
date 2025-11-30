package apap.ti._5.flight_2306211660_be.restdto.request.bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequestDTO {
    /**
     * Service reference ID (e.g., booking ID for flight service, package ID for tour package service).
     * This is used by the service to identify which resource to update after payment.
     */
    private String serviceReferenceId;
    
    private String customerId;
}

package apap.ti._5.flight_2306211660_be.restdto.request.bill;

import java.math.BigDecimal;

public class SaldoUpdateRequestDTO {

    private String userId;
    private BigDecimal amount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

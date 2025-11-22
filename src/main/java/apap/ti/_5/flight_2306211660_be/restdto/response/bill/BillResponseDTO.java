package apap.ti._5.flight_2306211660_be.restdto.response.bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BillResponseDTO {
    private UUID id;
    private String customerId;
    private String serviceName;
    private String serviceReferenceId;
    private String description;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentTimestamp;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceReferenceId() { return serviceReferenceId; }
    public void setServiceReferenceId(String serviceReferenceId) { this.serviceReferenceId = serviceReferenceId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    // dueDate removed: not part of response
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getPaymentTimestamp() { return paymentTimestamp; }
    public void setPaymentTimestamp(LocalDateTime paymentTimestamp) { this.paymentTimestamp = paymentTimestamp; }
}


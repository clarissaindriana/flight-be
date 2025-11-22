package apap.ti._5.flight_2306211660_be.restdto.request.bill;

import java.math.BigDecimal;
public class AddBillRequestDTO {

	private String customerId;
	private String serviceName;
	private String serviceReferenceId;
	private String description;
	private BigDecimal amount;

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
}

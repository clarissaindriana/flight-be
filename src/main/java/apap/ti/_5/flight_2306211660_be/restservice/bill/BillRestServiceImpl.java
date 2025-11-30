package apap.ti._5.flight_2306211660_be.restservice.bill;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.model.Bill;
import apap.ti._5.flight_2306211660_be.repository.BillRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.SaldoUpdateRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.UpdateBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.booking.ConfirmPaymentRequestDTO;

@Service
public class BillRestServiceImpl implements BillRestService {

    private final BillRepository billRepository;
    private final ProfileClient profileClient;
    private final Environment env;
    private final Logger logger = LoggerFactory.getLogger(BillRestServiceImpl.class);

    public BillRestServiceImpl(BillRepository billRepository, ProfileClient profileClient, Environment env) {
        this.billRepository = billRepository;
        this.profileClient = profileClient;
        this.env = env;
    }

    @Override
    public Bill createBill(AddBillRequestDTO req) throws Exception {
        // validations should be done at controller level; implement basic checks
        Bill bill = Bill.builder()
                .id(UUID.randomUUID())
                .customerId(req.getCustomerId())
                .serviceName(req.getServiceName())
                .serviceReferenceId(req.getServiceReferenceId())
                .description(req.getDescription())
                .amount(req.getAmount())
                .status(Bill.BillStatus.UNPAID)
                .build();

        return billRepository.save(bill);
    }

    @Override
    public List<Bill> getAllBills(String customerId, String serviceName, String status) {
        List<Bill> all = billRepository.findAll();
        return filterBills(all, customerId, serviceName, status);
    }

    @Override
    public List<Bill> getCustomerBills(String customerId, String status, String sortBy, String order) {
        List<Bill> list = billRepository.findByCustomerId(customerId);
        list = filterBills(list, customerId, null, status);

        // basic sorting
        if (sortBy != null) {
            boolean asc = !"desc".equalsIgnoreCase(order);
            if ("createdAt".equalsIgnoreCase(sortBy)) {
                list = list.stream().sorted((a,b) -> asc ? a.getCreatedAt().compareTo(b.getCreatedAt()) : b.getCreatedAt().compareTo(a.getCreatedAt())).collect(Collectors.toList());
            } else if ("serviceName".equalsIgnoreCase(sortBy)) {
                list = list.stream().sorted((a,b) -> asc ? a.getServiceName().compareTo(b.getServiceName()) : b.getServiceName().compareTo(a.getServiceName())).collect(Collectors.toList());
            }
        }

        return list;
    }

    @Override
    public List<Bill> getServiceBills(String serviceName, String customerId, String status) {
        List<Bill> list = billRepository.findByServiceName(serviceName);
        return filterBills(list, customerId, serviceName, status);
    }

    @Override
    public Bill getBillById(UUID id) {
        Optional<Bill> opt = billRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    @Transactional
    public Bill payBill(UUID id, String customerIdFromToken, String couponCode) throws Exception {
        Bill bill = getBillById(id);
        if (bill == null) {
            throw new IllegalArgumentException("No Bill Found");
        }

        // Validate bill status is UNPAID (must be checked first)
        if (bill.getStatus() != Bill.BillStatus.UNPAID) {
            throw new IllegalStateException("Bill is not in UNPAID status");
        }

        // Validate customerId matches
        if (!bill.getCustomerId().equals(customerIdFromToken)) {
            throw new SecurityException("Customer not allowed to pay this bill");
        }

        BigDecimal finalAmount = bill.getAmount();

        // TODO: If couponCode provided, call Loyalty Service to validate and compute discount
        // Currently skipped as per specification. Awaiting external Loyalty Service API.
        // Expected: Loyalty Service should accept couponCode and return discount percentage/amount
        // to reduce finalAmount before payment deduction.

        // Deduct saldo via /api/users/payment using SaldoUpdateRequestDTO to match external API
        SaldoUpdateRequestDTO saldoRequest = new SaldoUpdateRequestDTO();
        saldoRequest.setUserId(customerIdFromToken);
        saldoRequest.setAmount(finalAmount);

        logger.info("Processing payment for bill {} with customerId: {}, amount: {}", id, customerIdFromToken, finalAmount);

        ProfileClient.ProfileUserWrapper paymentResult = profileClient.paymentSaldo(saldoRequest);
        if (paymentResult == null || paymentResult.getData() == null) {
            logger.error("Payment API returned null or empty data for bill: {}", id);
            throw new RuntimeException("Failed to process payment with profile service");
        }
        
        logger.info("Payment deducted successfully for bill: {}", id);

        // Update bill status to PAID
        bill.setStatus(Bill.BillStatus.PAID);
        bill.setPaymentTimestamp(LocalDateTime.now());
        bill = billRepository.save(bill);

        // Send callback to origin service (best-effort)
        try {
            sendCallbackToOriginService(bill);
        } catch (Exception ex) {
            logger.warn("Callback to origin service failed: {}", ex.getMessage());
        }

        // TODO: Add loyalty points (1% of amount, rounded up) to customer via Loyalty Service

        return bill;
    }

    @Override
    @Transactional
    public Bill updateBill(UUID id, UpdateBillRequestDTO req) throws Exception {
        Bill bill = getBillById(id);
        if (bill == null) {
            throw new IllegalArgumentException("No Bill Found");
        }

        // Bill with status PAID cannot be updated
        if (bill.getStatus() == Bill.BillStatus.PAID) {
            throw new IllegalStateException("Bill with status PAID cannot be updated");
        }

        // Update mutable fields based on request
        bill.setCustomerId(req.getCustomerId());
        bill.setServiceName(req.getServiceName());
        bill.setServiceReferenceId(req.getServiceReferenceId());
        bill.setAmount(req.getAmount());

        return billRepository.save(bill);
    }

    private List<Bill> filterBills(List<Bill> source, String customerId, String serviceName, String status) {
        if (source == null) return new ArrayList<>();
        return source.stream().filter(b -> {
            if (customerId != null && !customerId.isBlank() && !customerId.equals(b.getCustomerId())) return false;
            if (serviceName != null && !serviceName.isBlank() && !serviceName.equalsIgnoreCase(b.getServiceName())) return false;
            if (status != null && !status.isBlank()) {
                try {
                    Bill.BillStatus st = Bill.BillStatus.valueOf(status.toUpperCase());
                    if (b.getStatus() != st) return false;
                } catch (Exception ex) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void sendCallbackToOriginService(Bill bill) {
        // Attempt to call origin service callback URL if configured via property
        String serviceNameLower = bill.getServiceName() == null ? "" : bill.getServiceName().toLowerCase();
        String key = "service.callback.base-url." + serviceNameLower;
        String base = env.getProperty(key, "");
        if (base == null || base.isBlank()) {
            logger.info("No callback base URL configured for service {} (property {}). Skipping callback.", bill.getServiceName(), key);
            return;
        }

        try {
            WebClient wc = WebClient.builder()
                    .baseUrl(base)
                    .build();

            // Build standard ConfirmPaymentRequestDTO callback payload
            ConfirmPaymentRequestDTO payload = new ConfirmPaymentRequestDTO(
                    bill.getId(),
                    bill.getCustomerId()
            );

            // Determine internal callback path per service.
            // For Flight (this service), expose /api/booking/payment/confirm.
            // For Tour Package Vendor, use /api/package/payment/confirm.
            // For other services, awaiting endpoint details.
            // TODO: Confirm callback endpoint paths with each service provider:
            // - Accommodation: awaiting internal API endpoint details from Accommodation Owner
            // - Vehicle Rental: awaiting internal API endpoint details from Rental Vendor
            // - Insurance: awaiting internal API endpoint details from Insurance Provider
            String path = switch (serviceNameLower) {
                case "flight" -> "/api/booking/payment/confirm";
                case "tourpackage" -> "/api/package/payment/confirm";
                case "accommodation" -> "/api/accommodation/payment/confirm"; // TODO: confirm with Accommodation service
                case "vehiclerental" -> "/api/rental/payment/confirm"; // TODO: confirm with Vehicle Rental service
                case "insurance" -> "/api/insurance/payment/confirm"; // TODO: confirm with Insurance service
                default -> "/payment/confirm"; // Default fallback
            };

            logger.info("Sending payment callback to: {}{} for bill: {}", base, path, bill.getId());

            wc.post()
                    .uri(path)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
                            logger.error("Callback HTTP error: status={}, body={}", clientResponse.statusCode(), body);
                            return reactor.core.publisher.Mono.error(new RuntimeException("HTTP " + clientResponse.statusCode() + ": " + body));
                        })
                    )
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            logger.info("ConfirmPayment callback sent successfully to {}{}", base, path);
        } catch (Exception ex) {
            logger.error("Failed to send confirmPayment callback to origin service {}: {} - {}", bill.getServiceName(), ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }
}
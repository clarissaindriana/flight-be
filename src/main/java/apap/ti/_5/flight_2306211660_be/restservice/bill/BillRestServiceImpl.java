package apap.ti._5.flight_2306211660_be.restservice.bill;

import java.math.BigDecimal;
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

import apap.ti._5.flight_2306211660_be.config.security.CurrentUser;
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

                // Fetch customer profile for balance (balance check is early step) via /api/users/detail.
                // Use email as primary search key (more stable across services), fall back to userId.
                String searchKey = CurrentUser.getEmail();
                if (searchKey == null || searchKey.isBlank()) {
                    searchKey = customerIdFromToken;
                }
        
                ProfileClient.ProfileUserWrapper detailWrapper = profileClient.getUserDetail(searchKey);
                if (detailWrapper == null || detailWrapper.getData() == null) {
                    // This will be treated as unexpected error by controller (500 with generic message)
                    throw new RuntimeException("Unable to fetch user profile");
                }

        ProfileClient.ProfileUser user = detailWrapper.getData();
        BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();

        // Check balance first (all processes must be blocked if balance insufficient)
        if (balance.compareTo(finalAmount) < 0) {
            // 400 with this exact message as required
            throw new IllegalStateException("User balance insufficient, please Top Up balance.");
        }

        // TODO: If couponCode provided, call Loyalty Service to validate and compute discount
        // Currently skipped as per specification (external API not yet available).

        // Deduct saldo via /api/users/payment using SaldoUpdateRequestDTO to match external API
        SaldoUpdateRequestDTO saldoRequest = new SaldoUpdateRequestDTO();
        saldoRequest.setUserId(customerIdFromToken);
        saldoRequest.setAmount(finalAmount);

        ProfileClient.ProfileUserWrapper paymentResult = profileClient.paymentSaldo(saldoRequest);
        if (paymentResult == null || paymentResult.getData() == null) {
            // Any failure here is considered unexpected so controller returns generic 500 message
            throw new RuntimeException("Failed to process payment with profile service");
        }

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
            WebClient wc = WebClient.create(base);

            // Build standard ConfirmPaymentRequestDTO callback payload
            ConfirmPaymentRequestDTO payload = new ConfirmPaymentRequestDTO(
                    bill.getId(),
                    bill.getCustomerId()
            );

            // Determine internal callback path per service.
            // For Flight (this service), expose /api/booking/confirmpayment.
            // For Tour Package Vendor, keep the provided endpoint.
            // For other services, we expect each to implement its own POST /confirmpayment.
            String path;
            if ("flight".equals(serviceNameLower)) {
                path = "/api/booking/payment/confirm";
            } else if ("tourpackage".equals(serviceNameLower)) {
                path = "/api/package/payment/confirm";
            } else {
                path = "/payment/confirm"; // TODO: align with each service's internal confirmPayment endpoint
            }

            wc.post()
                    .uri(path)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            logger.info("ConfirmPayment callback sent to {}{}", base, path);
        } catch (Exception ex) {
            logger.warn("Failed to send confirmPayment callback to origin service {}: {}", bill.getServiceName(), ex.getMessage());
        }
    }
}
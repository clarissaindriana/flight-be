package apap.ti._5.flight_2306211660_be.restcontroller.bill;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import apap.ti._5.flight_2306211660_be.config.security.CurrentUser;
import apap.ti._5.flight_2306211660_be.model.Bill;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.UpdateBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bill.BillResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.bill.BillRestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bill")
public class BillRestController {

    private final BillRestService billService;

    private static final List<String> ALLOWED_SERVICES = List.of("Flight", "Accommodation", "Insurance", "VehicleRental", "TourPackage");

    public BillRestController(BillRestService billService) {
        this.billService = billService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE','ACCOMMODATION_OWNER','RENTAL_VENDOR','INSURANCE_PROVIDER','TOUR_PACKAGE_VENDOR')")
    public ResponseEntity<BaseResponseDTO<BillResponseDTO>> createBill(@Valid @RequestBody AddBillRequestDTO req) {
        var base = new BaseResponseDTO<BillResponseDTO>();
        try {
            // validations
            if (req.getCustomerId() == null || req.getCustomerId().isBlank() || req.getServiceName() == null || req.getServiceName().isBlank() || req.getServiceReferenceId() == null || req.getServiceReferenceId().isBlank() || req.getDescription() == null || req.getDescription().isBlank() || req.getAmount() == null) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Missing required fields");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }
            if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Amount must be greater than zero");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }

            if (!ALLOWED_SERVICES.contains(req.getServiceName())) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Invalid serviceName");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }

            Bill bill = billService.createBill(req);
            BillResponseDTO dto = mapToDto(bill);

            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Bill created");
            base.setData(dto);
            base.setTimestamp(new java.util.Date());
            return ResponseEntity.ok(base);

        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{billId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE','ACCOMMODATION_OWNER','RENTAL_VENDOR','INSURANCE_PROVIDER','TOUR_PACKAGE_VENDOR')")
    public ResponseEntity<BaseResponseDTO<BillResponseDTO>> updateBill(
            @PathVariable UUID billId,
            @Valid @RequestBody UpdateBillRequestDTO req) {
        var base = new BaseResponseDTO<BillResponseDTO>();

        try {
            // Required field validations
            if (req.getCustomerId() == null || req.getCustomerId().isBlank()
                    || req.getServiceName() == null || req.getServiceName().isBlank()
                    || req.getServiceReferenceId() == null || req.getServiceReferenceId().isBlank()
                    || req.getAmount() == null) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Missing required fields");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }

            if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Amount must be greater than zero");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }

            if (!ALLOWED_SERVICES.contains(req.getServiceName())) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Invalid serviceName");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }

            Bill updated = billService.updateBill(billId, req);
            BillResponseDTO dto = mapToDto(updated);

            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Bill updated");
            base.setData(dto);
            base.setTimestamp(new java.util.Date());
            return ResponseEntity.ok(base);
        } catch (IllegalArgumentException ex) {
            // Bill not found
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException ex) {
            // Business rule violation (e.g., cannot update PAID bill)
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<BaseResponseDTO<List<BillResponseDTO>>> getAllBills(@RequestParam(required = false) String customerId, @RequestParam(required = false) String serviceName, @RequestParam(required = false) String status) {
        var base = new BaseResponseDTO<List<BillResponseDTO>>();
        List<Bill> list = billService.getAllBills(customerId, serviceName, status);
        if (list == null || list.isEmpty()) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        }

        List<BillResponseDTO> dtoList = list.stream().map(this::mapToDto).toList();
        base.setStatus(HttpStatus.OK.value());
        base.setMessage("OK");
        base.setData(dtoList);
        base.setTimestamp(new java.util.Date());
        return ResponseEntity.ok(base);
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<List<BillResponseDTO>>> getCustomerBills(@RequestParam(required = false) String customerId, @RequestParam(required = false) String status, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String order) {
        var base = new BaseResponseDTO<List<BillResponseDTO>>();
        
        // Get customerId from JWT token (customer must view their own bills only)
        String authenticatedCustomerId = CurrentUser.getUserId();
        if (authenticatedCustomerId == null || authenticatedCustomerId.isBlank()) {
            base.setStatus(HttpStatus.UNAUTHORIZED.value());
            base.setMessage("User not authenticated");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.UNAUTHORIZED);
        }
        
        // If customerId parameter provided, validate it matches JWT token
        if (customerId != null && !customerId.isBlank() && !customerId.equals(authenticatedCustomerId)) {
            base.setStatus(HttpStatus.FORBIDDEN.value());
            base.setMessage("Can only view your own bills");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
        }
        
        // Use authenticated user's ID
        List<Bill> list = billService.getCustomerBills(authenticatedCustomerId, status, sortBy, order);
        if (list == null || list.isEmpty()) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        }
        List<BillResponseDTO> dtoList = list.stream().map(this::mapToDto).toList();
        base.setStatus(HttpStatus.OK.value());
        base.setMessage("OK");
        base.setData(dtoList);
        base.setTimestamp(new java.util.Date());
        return ResponseEntity.ok(base);
    }

    @GetMapping("/{serviceName}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','FLIGHT_AIRLINE','ACCOMMODATION_OWNER','RENTAL_VENDOR','INSURANCE_PROVIDER','TOUR_PACKAGE_VENDOR')")
    public ResponseEntity<BaseResponseDTO<List<BillResponseDTO>>> getServiceBills(@PathVariable String serviceName, @RequestParam(required = false) String customerId, @RequestParam(required = false) String status) {
        var base = new BaseResponseDTO<List<BillResponseDTO>>();
        
        // Enforce role-to-service matching: caller role must match the serviceName (unless superadmin)
        String callerRole = CurrentUser.getRole();
        if (callerRole != null && !callerRole.equalsIgnoreCase("ROLE_SUPERADMIN")) {
            // Map serviceName to expected role
            String expectedRole = mapServiceNameToRole(serviceName);
            if (expectedRole == null || !callerRole.equalsIgnoreCase(expectedRole)) {
                base.setStatus(HttpStatus.FORBIDDEN.value());
                base.setMessage("Forbidden: can only access bills for your service");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
            }
        }
        
        List<Bill> list = billService.getServiceBills(serviceName, customerId, status);
        if (list == null || list.isEmpty()) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        }

        List<BillResponseDTO> dtoList = list.stream().map(this::mapToDto).toList();
        base.setStatus(HttpStatus.OK.value());
        base.setMessage("OK");
        base.setData(dtoList);
        base.setTimestamp(new java.util.Date());
        return ResponseEntity.ok(base);
    }
    
    private String mapServiceNameToRole(String serviceName) {
        if (serviceName == null) return null;
        return switch (serviceName.toLowerCase()) {
            case "flight" -> "ROLE_FLIGHT_AIRLINE";
            case "accommodation" -> "ROLE_ACCOMMODATION_OWNER";
            case "vehiclerental" -> "ROLE_RENTAL_VENDOR";
            case "insurance" -> "ROLE_INSURANCE_PROVIDER";
            case "tourpackage" -> "ROLE_TOUR_PACKAGE_VENDOR";
            default -> null;
        };
    }

    @GetMapping("/detail/{billId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','SUPERADMIN','FLIGHT_AIRLINE','ACCOMMODATION_OWNER','RENTAL_VENDOR','INSURANCE_PROVIDER','TOUR_PACKAGE_VENDOR')")
    public ResponseEntity<BaseResponseDTO<BillResponseDTO>> getBillDetail(@PathVariable UUID billId) {
        var base = new BaseResponseDTO<BillResponseDTO>();
        Bill bill = billService.getBillById(billId);
        if (bill == null) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        }

        // role-based visibility
        String callerRole = CurrentUser.getRole();
        String callerUserId = CurrentUser.getUserId();
        
        // SUPERADMIN can view all bills
        if (callerRole != null && callerRole.equalsIgnoreCase("ROLE_SUPERADMIN")) {
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("OK");
            base.setData(mapToDto(bill));
            base.setTimestamp(new java.util.Date());
            return ResponseEntity.ok(base);
        }
        
        // CUSTOMER can view their own bills only
        if (callerRole != null && callerRole.equalsIgnoreCase("ROLE_CUSTOMER")) {
            if (callerUserId == null || !callerUserId.equals(bill.getCustomerId())) {
                base.setStatus(HttpStatus.FORBIDDEN.value());
                base.setMessage("Forbidden");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
            }
        } else {
            // Service role: ensure service matches
            String expectedRole = mapServiceNameToRole(bill.getServiceName());
            if (expectedRole == null || callerRole == null || !callerRole.equalsIgnoreCase(expectedRole)) {
                base.setStatus(HttpStatus.FORBIDDEN.value());
                base.setMessage("Forbidden");
                base.setTimestamp(new java.util.Date());
                return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
            }
        }

        base.setStatus(HttpStatus.OK.value());
        base.setMessage("OK");
        base.setData(mapToDto(bill));
        base.setTimestamp(new java.util.Date());
        return ResponseEntity.ok(base);
    }

    @PostMapping("/{billId}/pay")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<BaseResponseDTO<BillResponseDTO>> payBill(
            @PathVariable UUID billId,
            @RequestBody(required = false) ConfirmPaymentRequestDTO req) {
        var base = new BaseResponseDTO<BillResponseDTO>();
        String callerUserId = CurrentUser.getUserId();
        String customerIdFromBody = (req != null ? req.getCustomerId() : null);
        String coupon = null; // TODO: Integrate coupon/loyalty service when available

        try {
            // Validate customerId from request (if provided) matches JWT token
            if (customerIdFromBody != null && !customerIdFromBody.isBlank()
                    && !customerIdFromBody.equals(callerUserId)) {
                throw new SecurityException("Customer ID mismatch");
            }

            // Validate customerId from JWT matches bill's customerId (early check)
            Bill bill = billService.getBillById(billId);
            if (bill == null) throw new IllegalArgumentException("No Bill Found");
            if (!bill.getCustomerId().equals(callerUserId)) {
                throw new SecurityException("Customer ID mismatch");
            }

            Bill paid = billService.payBill(billId, callerUserId, coupon);
            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Payment successful");
            base.setData(mapToDto(paid));
            base.setTimestamp(new java.util.Date());
            return ResponseEntity.ok(base);
        } catch (IllegalArgumentException ex) {
            base.setStatus(HttpStatus.NOT_FOUND.value());
            base.setMessage("No Bill Found");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.NOT_FOUND);
        } catch (SecurityException ex) {
            base.setStatus(HttpStatus.FORBIDDEN.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.FORBIDDEN);
        } catch (IllegalStateException ex) {
            // Includes insufficient balance ("User balance insufficient, please Top Up balance.")
            base.setStatus(HttpStatus.BAD_REQUEST.value());
            base.setMessage(ex.getMessage());
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Payment Failed. An unexpected error occurred. Please try again later.");
            base.setTimestamp(new java.util.Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private BillResponseDTO mapToDto(Bill b) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(b.getId());
        dto.setCustomerId(b.getCustomerId());
        dto.setServiceName(b.getServiceName());
        dto.setServiceReferenceId(b.getServiceReferenceId());
        dto.setDescription(b.getDescription());
        dto.setAmount(b.getAmount());
        dto.setStatus(b.getStatus().name());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setUpdatedAt(b.getUpdatedAt());
        dto.setPaymentTimestamp(b.getPaymentTimestamp());
        return dto;
    }
}

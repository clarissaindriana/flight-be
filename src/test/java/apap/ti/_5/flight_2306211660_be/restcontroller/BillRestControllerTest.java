package apap.ti._5.flight_2306211660_be.restcontroller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.flight_2306211660_be.config.security.CurrentUser;
import apap.ti._5.flight_2306211660_be.model.Bill;
import apap.ti._5.flight_2306211660_be.restcontroller.bill.BillRestController;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.ConfirmPaymentRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.UpdateBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.response.bill.BillResponseDTO;
import apap.ti._5.flight_2306211660_be.restservice.bill.BillRestService;

@ExtendWith(MockitoExtension.class)
class BillRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BillRestService billRestService;

    @BeforeEach
    void setup() {
        BillRestController controller = new BillRestController(billRestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilter((request, response, chain) -> {
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("cust1", "password",
                            java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
                    chain.doFilter(request, response);
                })
                .build();
    }

    private Bill sampleBill(UUID id, String customerId, String serviceName, String serviceRef, String desc, BigDecimal amount, Bill.BillStatus status) {
        return Bill.builder()
                .id(id)
                .customerId(customerId)
                .serviceName(serviceName)
                .serviceReferenceId(serviceRef)
                .description(desc)
                .amount(amount)
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== POST /api/bill/create Tests ====================

    @Test
    @DisplayName("POST /api/bill/create returns 200 when valid")
    void createBill_valid() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));
        var bill = sampleBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "Flight booking", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);

        when(billRestService.createBill(any(AddBillRequestDTO.class))).thenReturn(bill);

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.customerId").value("cust1"));
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when customerId is null")
    void createBill_nullCustomerId() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId(null);
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when customerId is blank")
    void createBill_blankCustomerId() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when serviceName is null")
    void createBill_nullServiceName() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName(null);
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when amount is null")
    void createBill_nullAmount() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(null);

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when amount is zero")
    void createBill_zeroAmount() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when amount is negative")
    void createBill_negativeAmount() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(-50.0));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 400 when invalid serviceName")
    void createBill_invalidService() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Invalid");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid serviceName"));

        verify(billRestService, never()).createBill(any());
    }

    @Test
    @DisplayName("POST /api/bill/create returns 500 on exception")
    void createBill_exception() throws Exception {
        var req = new AddBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setDescription("Flight booking");
        req.setAmount(BigDecimal.valueOf(100.0));

        when(billRestService.createBill(any(AddBillRequestDTO.class))).thenThrow(new RuntimeException("db error"));

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error: db error"));
    }

    // ==================== PUT /api/bill/update/{billId} Tests ====================

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 200 when valid")
    void updateBill_valid() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = new UpdateBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setAmount(BigDecimal.valueOf(150.0));
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "Updated", BigDecimal.valueOf(150.0), Bill.BillStatus.UNPAID);

        when(billRestService.updateBill(eq(billId), any(UpdateBillRequestDTO.class))).thenReturn(bill);

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.amount").value(150.0));
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 400 when missing required fields")
    void updateBill_missingFields() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = new UpdateBillRequestDTO();
        req.setCustomerId(null);
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setAmount(BigDecimal.valueOf(100.0));

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing required fields"));

        verify(billRestService, never()).updateBill(any(), any());
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 400 when amount <= 0")
    void updateBill_invalidAmount() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = new UpdateBillRequestDTO();
        req.setCustomerId("cust1");
        req.setServiceName("Flight");
        req.setServiceReferenceId("ref1");
        req.setAmount(BigDecimal.ZERO);

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));

        verify(billRestService, never()).updateBill(any(), any());
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 400 when invalid serviceName")
    void updateBill_invalidService() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = UpdateBillRequestDTO.builder()
                .customerId("cust1")
                .serviceName("Invalid")
                .serviceReferenceId("ref1")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid serviceName"));

        verify(billRestService, never()).updateBill(any(), any());
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 404 when bill not found")
    void updateBill_notFound() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = UpdateBillRequestDTO.builder()
                .customerId("cust1")
                .serviceName("Flight")
                .serviceReferenceId("ref1")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        when(billRestService.updateBill(eq(billId), any(UpdateBillRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("No Bill Found"));

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No Bill Found"));
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 400 when bill PAID")
    void updateBill_paid() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = UpdateBillRequestDTO.builder()
                .customerId("cust1")
                .serviceName("Flight")
                .serviceReferenceId("ref1")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        when(billRestService.updateBill(eq(billId), any(UpdateBillRequestDTO.class)))
                .thenThrow(new IllegalStateException("Bill with status PAID cannot be updated"));

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bill with status PAID cannot be updated"));
    }

    @Test
    @DisplayName("PUT /api/bill/update/{billId} returns 500 on exception")
    void updateBill_exception() throws Exception {
        UUID billId = UUID.randomUUID();
        var req = UpdateBillRequestDTO.builder()
                .customerId("cust1")
                .serviceName("Flight")
                .serviceReferenceId("ref1")
                .amount(BigDecimal.valueOf(100.0))
                .build();

        when(billRestService.updateBill(eq(billId), any(UpdateBillRequestDTO.class)))
                .thenThrow(new RuntimeException("db error"));

        mockMvc.perform(put("/api/bill/update/{billId}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error: db error"));
    }

    // ==================== GET /api/bill Tests ====================

    @Test
    @DisplayName("GET /api/bill returns 200 with bills")
    void getAllBills_found() throws Exception {
        var bill1 = sampleBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = sampleBill(UUID.randomUUID(), "cust2", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRestService.getAllBills(null, null, null)).thenReturn(Arrays.asList(bill1, bill2));

        mockMvc.perform(get("/api/bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/bill returns 404 when no bills")
    void getAllBills_notFound() throws Exception {
        when(billRestService.getAllBills(null, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bill"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No Bill Found"));
    }

    // ==================== GET /api/bill/customer Tests ====================

    @Test
    @DisplayName("GET /api/bill/customer returns 200 when user authenticated and bills found")
    void getCustomerBills_valid() throws Exception {
        var bill1 = sampleBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getCustomerBills("cust1", null, null, null)).thenReturn(Arrays.asList(bill1));

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/customer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Test
    @DisplayName("GET /api/bill/customer returns 401 when user not authenticated")
    void getCustomerBills_notAuthenticated() throws Exception {
        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn(null);

            mockMvc.perform(get("/api/bill/customer"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("User not authenticated"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/customer returns 403 when customerId parameter doesn't match JWT")
    void getCustomerBills_forbiddenMismatch() throws Exception {
        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/customer?customerId=cust2"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Can only view your own bills"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/customer returns 404 when no bills found")
    void getCustomerBills_notFound() throws Exception {
        when(billRestService.getCustomerBills("cust1", null, null, null)).thenReturn(Collections.emptyList());

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/customer"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("No Bill Found"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/customer returns 200 with sorting")
    void getCustomerBills_withSorting() throws Exception {
        var bill1 = sampleBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getCustomerBills("cust1", null, "createdAt", "asc")).thenReturn(Arrays.asList(bill1));

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/customer?sortBy=createdAt&order=asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    // ==================== GET /api/bill/{serviceName} Tests ====================

    @Test
    @DisplayName("GET /api/bill/{serviceName} returns 200 when valid")
    void getServiceBills_valid() throws Exception {
        var bill1 = sampleBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getServiceBills("Flight", null, null)).thenReturn(Arrays.asList(bill1));

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_SUPERADMIN");

            mockMvc.perform(get("/api/bill/Flight"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Test
    @DisplayName("GET /api/bill/{serviceName} returns 403 when role doesn't match service")
    void getServiceBills_forbiddenRole() throws Exception {
        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_ACCOMMODATION_OWNER");

            mockMvc.perform(get("/api/bill/Flight"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Forbidden: can only access bills for your service"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/{serviceName} returns 404 when no bills found")
    void getServiceBills_notFound() throws Exception {
        when(billRestService.getServiceBills("Flight", null, null)).thenReturn(Collections.emptyList());

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_SUPERADMIN");

            mockMvc.perform(get("/api/bill/Flight"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("No Bill Found"));
        }
    }

    // ==================== GET /api/bill/detail/{billId} Tests ====================

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 200 for SUPERADMIN")
    void getBillDetail_superadmin() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_SUPERADMIN");
            mocked.when(CurrentUser::getUserId).thenReturn("admin");

            mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 200 for CUSTOMER viewing own bill")
    void getBillDetail_customerOwn() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 403 for CUSTOMER viewing other's bill")
    void getBillDetail_customerOther() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust2", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_CUSTOMER");
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Forbidden"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 200 for matching service role")
    void getBillDetail_serviceRole() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_FLIGHT_AIRLINE");
            mocked.when(CurrentUser::getUserId).thenReturn("airline1");

            mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 403 for mismatched service role")
    void getBillDetail_mismatchedServiceRole() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getRole).thenReturn("ROLE_ACCOMMODATION_OWNER");
            mocked.when(CurrentUser::getUserId).thenReturn("accommodation1");

            mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Forbidden"));
        }
    }

    @Test
    @DisplayName("GET /api/bill/detail/{billId} returns 404 when bill not found")
    void getBillDetail_notFound() throws Exception {
        UUID billId = UUID.randomUUID();
        when(billRestService.getBillById(billId)).thenReturn(null);

        mockMvc.perform(get("/api/bill/detail/{billId}", billId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No Bill Found"));
    }

    // ==================== POST /api/bill/{billId}/pay Tests ====================

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 200 when payment successful")
    void payBill_success() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var paidBill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.PAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);
        when(billRestService.payBill(eq(billId), eq("cust1"), any())).thenReturn(paidBill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            var req = new ConfirmPaymentRequestDTO();
            req.setCustomerId("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Payment successful"));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 403 when customer ID mismatch from body")
    void payBill_customerIdMismatchBody() throws Exception {
        UUID billId = UUID.randomUUID();

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            var req = new ConfirmPaymentRequestDTO();
            req.setCustomerId("cust2");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Customer ID mismatch"));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 404 when bill not found")
    void payBill_billNotFound() throws Exception {
        UUID billId = UUID.randomUUID();

        when(billRestService.getBillById(billId)).thenReturn(null);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ConfirmPaymentRequestDTO())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("No Bill Found"));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 403 when bill customer mismatch")
    void payBill_billCustomerMismatch() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust2", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ConfirmPaymentRequestDTO())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Customer ID mismatch"));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 400 when insufficient balance")
    void payBill_insufficientBalance() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);
        when(billRestService.payBill(eq(billId), eq("cust1"), any()))
                .thenThrow(new IllegalStateException("User balance insufficient, please Top Up balance."));

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ConfirmPaymentRequestDTO())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("User balance insufficient, please Top Up balance."));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 500 on unexpected exception")
    void payBill_exception() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);
        when(billRestService.payBill(eq(billId), eq("cust1"), any()))
                .thenThrow(new RuntimeException("unexpected error"));

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new ConfirmPaymentRequestDTO())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Payment Failed. An unexpected error occurred. Please try again later."));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 200 when request body is null")
    void payBill_nullBody() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var paidBill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.PAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);
        when(billRestService.payBill(eq(billId), eq("cust1"), any())).thenReturn(paidBill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }

    @Test
    @DisplayName("POST /api/bill/{billId}/pay returns 200 when customerId matches JWT")
    void payBill_customIdMatchesJwt() throws Exception {
        UUID billId = UUID.randomUUID();
        var bill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var paidBill = sampleBill(billId, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.PAID);

        when(billRestService.getBillById(billId)).thenReturn(bill);
        when(billRestService.payBill(eq(billId), eq("cust1"), any())).thenReturn(paidBill);

        try (var mocked = mockStatic(CurrentUser.class)) {
            mocked.when(CurrentUser::getUserId).thenReturn("cust1");

            var req = new ConfirmPaymentRequestDTO();
            req.setCustomerId("cust1");

            mockMvc.perform(post("/api/bill/{billId}/pay", billId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }
}

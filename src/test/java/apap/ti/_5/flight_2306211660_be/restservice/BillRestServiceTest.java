package apap.ti._5.flight_2306211660_be.restservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.model.Bill;
import apap.ti._5.flight_2306211660_be.repository.BillRepository;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.UpdateBillRequestDTO;
import apap.ti._5.flight_2306211660_be.restservice.bill.BillRestServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BillRestServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ProfileClient profileClient;

    @Mock
    private org.springframework.core.env.Environment env;

    @InjectMocks
    private BillRestServiceImpl billRestService;

    private Bill buildBill(UUID id, String customerId, String serviceName, String serviceRef, String desc, BigDecimal amount, Bill.BillStatus status) {
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

    @Test
    void createBill_success() throws Exception {
        var dto = new AddBillRequestDTO();
        dto.setCustomerId("cust1");
        dto.setServiceName("Flight");
        dto.setServiceReferenceId("ref1");
        dto.setDescription("Flight booking");
        dto.setAmount(BigDecimal.valueOf(100.0));

        var saved = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "Flight booking", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.save(any(Bill.class))).thenReturn(saved);

        Bill result = billRestService.createBill(dto);

        assertNotNull(result);
        assertEquals("cust1", result.getCustomerId());
        assertEquals("Flight", result.getServiceName());
        assertEquals(BigDecimal.valueOf(100.0), result.getAmount());
        assertEquals(Bill.BillStatus.UNPAID, result.getStatus());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void getAllBills_success() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust2", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getAllBills(null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(billRepository).findAll();
    }

    @Test
    void getCustomerBills_success() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill1));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cust1", result.get(0).getCustomerId());
        verify(billRepository).findByCustomerId("cust1");
    }

    @Test
    void getServiceBills_success() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findByServiceName("Flight")).thenReturn(Arrays.asList(bill1));

        List<Bill> result = billRestService.getServiceBills("Flight", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Flight", result.get(0).getServiceName());
        verify(billRepository).findByServiceName("Flight");
    }

    @Test
    void getBillById_found() {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));

        Bill result = billRestService.getBillById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(billRepository).findById(id);
    }

    @Test
    void getBillById_notFound() {
        UUID id = UUID.randomUUID();
        when(billRepository.findById(id)).thenReturn(Optional.empty());

        Bill result = billRestService.getBillById(id);

        assertNull(result);
        verify(billRepository).findById(id);
    }

    @Test
    void updateBill_success() throws Exception {
        UUID id = UUID.randomUUID();
        var existing = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var dto = new UpdateBillRequestDTO();
        dto.setCustomerId("cust1");
        dto.setServiceName("Flight");
        dto.setServiceReferenceId("ref1");
        dto.setAmount(BigDecimal.valueOf(150.0));

        when(billRepository.findById(id)).thenReturn(Optional.of(existing));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billRestService.updateBill(id, dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.0), result.getAmount());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void updateBill_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = new UpdateBillRequestDTO();
        dto.setCustomerId("cust1");
        dto.setServiceName("Flight");
        dto.setServiceReferenceId("ref1");
        dto.setAmount(BigDecimal.valueOf(100.0));

        when(billRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> billRestService.updateBill(id, dto));
        verify(billRepository, never()).save(any());
    }

    @Test
    void updateBill_paid_throws() throws Exception {
        UUID id = UUID.randomUUID();
        var existing = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.PAID);
        var dto = new UpdateBillRequestDTO();
        dto.setCustomerId("cust1");
        dto.setServiceName("Flight");
        dto.setServiceReferenceId("ref1");
        dto.setAmount(BigDecimal.valueOf(150.0));

        when(billRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> billRestService.updateBill(id, dto));
        verify(billRepository, never()).save(any());
    }

    @Test
    void payBill_success() throws Exception {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));
        ProfileClient.ProfileUserWrapper mockWrapper = mock(ProfileClient.ProfileUserWrapper.class);
        ProfileClient.ProfileUser mockUser = new ProfileClient.ProfileUser();
        when(mockWrapper.getData()).thenReturn(mockUser);
        when(profileClient.paymentSaldo(any())).thenReturn(mockWrapper);
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billRestService.payBill(id, "cust1", null);

        assertNotNull(result);
        assertEquals(Bill.BillStatus.PAID, result.getStatus());
        assertNotNull(result.getPaymentTimestamp());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void payBill_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(billRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> billRestService.payBill(id, "cust1", null));
    }

    @Test
    void payBill_notUnpaid() throws Exception {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.PAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));

        assertThrows(IllegalStateException.class, () -> billRestService.payBill(id, "cust1", null));
    }

    @Test
    void payBill_customerMismatch() throws Exception {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust2", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));

        assertThrows(SecurityException.class, () -> billRestService.payBill(id, "cust1", null));
    }

    @Test
    void payBill_profileClientFailure() throws Exception {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));
        when(profileClient.paymentSaldo(any())).thenReturn(null); // failure

        assertThrows(RuntimeException.class, () -> billRestService.payBill(id, "cust1", null));
    }

    @Test
    void getAllBills_withFilters() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust2", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        var bill3 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref3", "desc3", BigDecimal.valueOf(150.0), Bill.BillStatus.PAID);
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill1, bill2, bill3));

        List<Bill> result = billRestService.getAllBills("cust1", "Flight", "UNPAID");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cust1", result.get(0).getCustomerId());
        assertEquals("Flight", result.get(0).getServiceName());
        assertEquals(Bill.BillStatus.UNPAID, result.get(0).getStatus());
    }

    @Test
    void getAllBills_withServiceFilter() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust2", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getAllBills(null, "Accommodation", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Accommodation", result.get(0).getServiceName());
    }

    @Test
    void getAllBills_withStatusFilter() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust2", "Flight", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getAllBills(null, null, "PAID");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Bill.BillStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void getAllBills_withInvalidStatus() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findAll()).thenReturn(Arrays.asList(bill1));

        List<Bill> result = billRestService.getAllBills(null, null, "INVALID_STATUS");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getCustomerBills_withStatusFilter() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust1", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getCustomerBills("cust1", "PAID", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Bill.BillStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void getCustomerBills_sortByCreatedAtAsc() {
        LocalDateTime now = LocalDateTime.now();
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        bill1.setCreatedAt(now.minusDays(2));
        var bill2 = buildBill(UUID.randomUUID(), "cust1", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        bill2.setCreatedAt(now.minusDays(1));
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill2, bill1));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, "createdAt", "asc");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(now.minusDays(2), result.get(0).getCreatedAt());
        assertEquals(now.minusDays(1), result.get(1).getCreatedAt());
    }

    @Test
    void getCustomerBills_sortByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        bill1.setCreatedAt(now.minusDays(2));
        var bill2 = buildBill(UUID.randomUUID(), "cust1", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        bill2.setCreatedAt(now.minusDays(1));
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(now.minusDays(1), result.get(0).getCreatedAt());
        assertEquals(now.minusDays(2), result.get(1).getCreatedAt());
    }

    @Test
    void getCustomerBills_sortByServiceNameAsc() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust1", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill1, bill2));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, "serviceName", "asc");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Accommodation", result.get(0).getServiceName());
        assertEquals("Flight", result.get(1).getServiceName());
    }

    @Test
    void getCustomerBills_sortByServiceNameDesc() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust1", "Accommodation", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill2, bill1));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, "serviceName", "desc");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Flight", result.get(0).getServiceName());
        assertEquals("Accommodation", result.get(1).getServiceName());
    }

    @Test
    void getCustomerBills_unknownSortBy() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        when(billRepository.findByCustomerId("cust1")).thenReturn(Arrays.asList(bill1));

        List<Bill> result = billRestService.getCustomerBills("cust1", null, "unknown", null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getServiceBills_withFilters() {
        var bill1 = buildBill(UUID.randomUUID(), "cust1", "Flight", "ref1", "desc1", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var bill2 = buildBill(UUID.randomUUID(), "cust2", "Flight", "ref2", "desc2", BigDecimal.valueOf(200.0), Bill.BillStatus.PAID);
        var bill3 = buildBill(UUID.randomUUID(), "cust3", "Flight", "ref3", "desc3", BigDecimal.valueOf(150.0), Bill.BillStatus.UNPAID);
        when(billRepository.findByServiceName("Flight")).thenReturn(Arrays.asList(bill1, bill2, bill3));

        List<Bill> result = billRestService.getServiceBills("Flight", "cust1", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cust1", result.get(0).getCustomerId());
    }

    @Test
    void updateBill_success_withDescription() throws Exception {
        UUID id = UUID.randomUUID();
        var existing = buildBill(id, "cust1", "Flight", "ref1", "old desc", BigDecimal.valueOf(100.0), Bill.BillStatus.UNPAID);
        var dto = new UpdateBillRequestDTO();
        dto.setCustomerId("cust1");
        dto.setServiceName("Flight");
        dto.setServiceReferenceId("ref1");
        dto.setAmount(BigDecimal.valueOf(150.0));

        when(billRepository.findById(id)).thenReturn(Optional.of(existing));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billRestService.updateBill(id, dto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.0), result.getAmount());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void getServiceBills_emptyList() {
        when(billRepository.findByServiceName("Flight")).thenReturn(Collections.emptyList());

        List<Bill> result = billRestService.getServiceBills("Flight", null, null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getCustomerBills_emptyList() {
        when(billRepository.findByCustomerId("cust1")).thenReturn(Collections.emptyList());

        List<Bill> result = billRestService.getCustomerBills("cust1", null, null, null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void payBill_withInsufficientBalance() throws Exception {
        UUID id = UUID.randomUUID();
        var bill = buildBill(id, "cust1", "Flight", "ref1", "desc", BigDecimal.valueOf(1000.0), Bill.BillStatus.UNPAID);
        when(billRepository.findById(id)).thenReturn(Optional.of(bill));
        
        ProfileClient.ProfileUserWrapper mockWrapper = mock(ProfileClient.ProfileUserWrapper.class);
        ProfileClient.ProfileUser mockUser = new ProfileClient.ProfileUser();
        mockUser.setBalance(BigDecimal.valueOf(100.0)); // Less than bill amount
        when(mockWrapper.getData()).thenReturn(mockUser);
        when(profileClient.paymentSaldo(any())).thenThrow(new IllegalStateException("User balance insufficient, please Top Up balance."));

        assertThrows(IllegalStateException.class, () -> billRestService.payBill(id, "cust1", null));
    }

}

package apap.ti._5.flight_2306211660_be.restservice.bill;

import java.util.List;
import java.util.UUID;

import apap.ti._5.flight_2306211660_be.model.Bill;
import apap.ti._5.flight_2306211660_be.restdto.request.bill.AddBillRequestDTO;

public interface BillRestService {

    Bill createBill(AddBillRequestDTO req) throws Exception;

    List<Bill> getAllBills(String customerId, String serviceName, String status);

    List<Bill> getCustomerBills(String customerId, String status, String sortBy, String order);

    List<Bill> getServiceBills(String serviceName, String customerId, String status);

    Bill getBillById(UUID id);

    Bill payBill(UUID id, String customerIdFromToken, String couponCode) throws Exception;
}

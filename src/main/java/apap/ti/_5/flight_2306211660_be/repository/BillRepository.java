package apap.ti._5.flight_2306211660_be.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.flight_2306211660_be.model.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {

    List<Bill> findByCustomerId(String customerId);

    List<Bill> findByServiceName(String serviceName);

    List<Bill> findByCustomerIdAndStatus(String customerId, Bill.BillStatus status);

    List<Bill> findByServiceNameAndStatus(String serviceName, Bill.BillStatus status);
}

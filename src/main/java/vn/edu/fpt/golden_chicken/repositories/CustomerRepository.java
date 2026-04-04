package vn.edu.fpt.golden_chicken.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByUserFullNameContainingIgnoreCase(String fullName);

    @Modifying
    @Transactional
    @Query(value = "insert into customer_vouchers (customer_id, voucher_id, status) select c.user_id, :voucherId, 0 from customers c", nativeQuery = true)
    void distributeVoucherToAllActiveCustomers(@Param("voucherId") Long id);
}

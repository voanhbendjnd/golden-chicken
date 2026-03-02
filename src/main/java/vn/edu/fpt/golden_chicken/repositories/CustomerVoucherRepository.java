package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;

public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {
    List<CustomerVoucher> findByCustomer(Customer customer);

    List<CustomerVoucher> findByCustomerOrderByRedeemedAtDesc(Customer customer);

    List<CustomerVoucher> findByStatus(StatusVoucher status);
}

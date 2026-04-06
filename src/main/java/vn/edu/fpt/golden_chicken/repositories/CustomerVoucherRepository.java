package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.utils.constants.StatusVoucher;

public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {
    List<CustomerVoucher> findByCustomer(Customer customer);

    List<CustomerVoucher> findByCustomerOrderByRedeemedAtDesc(Customer customer);

    List<CustomerVoucher> findByStatus(StatusVoucher status);

    boolean existsByVoucher_Id(Long voucherId);

    List<CustomerVoucher> findByCustomer_IdAndStatus(Long customerId, StatusVoucher status);

    CustomerVoucher findFirstByCustomer_IdAndVoucher_CodeAndStatusOrderByIdDesc(
            Long customerId,
            String code,
            StatusVoucher status);

    @Query("SELECT cv FROM CustomerVoucher cv " +
            "JOIN FETCH cv.voucher v " +
            "JOIN FETCH cv.customer c " +
            "WHERE v.exchangeable = true AND c.id = :customer_id " +
            "ORDER BY cv.redeemedAt DESC")
    List<CustomerVoucher> findAllCustomerAndIsAllow(@Param("customer_id") Long customerId);
}

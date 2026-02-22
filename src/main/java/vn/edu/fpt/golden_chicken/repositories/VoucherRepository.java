package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;


import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByIsDeletedFalse();
    @Query("""
            SELECT v FROM Voucher v
            WHERE v.isDeleted = false
            AND v.status = 'ACTIVE'
            AND v.exchangeable = true
            AND v.startAt <= CURRENT_TIMESTAMP
            AND v.endAt >= CURRENT_TIMESTAMP
            """)
    List<Voucher> findAvailableForExchange();
    boolean existsByCode(String code);
}


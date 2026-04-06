package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;

import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.status = 'ACTIVE'
            AND v.exchangeable = true
            AND v.startAt <= CURRENT_TIMESTAMP
            AND v.endAt >= CURRENT_TIMESTAMP
            """)
    List<Voucher> findAvailableForExchange();

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.status = 'ACTIVE'
            AND v.exchangeable = false
            """)
    List<Voucher> findAvailableForIsNotExchange();

    boolean existsByCodeIgnoreCase(String code);

    List<Voucher> findAllByStatus(String status);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<Voucher> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}

package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Order;
import vn.edu.fpt.golden_chicken.domain.entity.Staff;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentStatus;

@Repository
@SuppressWarnings("null")
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
        boolean existsByCustomerId(Long id);

        boolean existsByShipperId(Long id);

        @EntityGraph(attributePaths = { "orderItems", "orderItems.product" })
        Page<Order> findAll(Specification<Order> spec, Pageable pageable);

        Page<Order> findByStatusAndShipperIsNull(OrderStatus status, Pageable pageable);

        Page<Order> findByShipperAndStatus(Staff shipper, OrderStatus status, Pageable pageable);

        Page<Order> findByShipperAndStatusIn(Staff shipper, java.util.List<OrderStatus> statuses, Pageable pageable);

        long countByShipperAndStatus(Staff shipper, OrderStatus status);

        long countByShipperAndStatusAndUpdatedAtBetween(Staff shipper, OrderStatus status,
                        java.time.LocalDateTime start, java.time.LocalDateTime end);

        // total orders assigned to a shipper regardless of status
        long countByShipper(Staff shipper);

        @Query("select coalesce(sum(o.finalAmount),0) from Order o where o.shipper = ?1 and o.paymentMethod = ?2 and o.paymentStatus = ?3")
        java.math.BigDecimal sumFinalAmountByShipperAndPaymentMethodAndPaymentStatus(Staff shipper,
                        PaymentMethod paymentMethod, PaymentStatus paymentStatus);

        @Query(value = "SELECT MONTH(o.updated_at) as month, SUM(o.final_amount) as revenue " +
                        "FROM orders o " +
                        "WHERE o.status = 'COMPLETED' " +
                        "AND YEAR(o.updated_at) = YEAR(GETDATE()) " +
                        "GROUP BY MONTH(o.updated_at) " +
                        "ORDER BY MONTH(o.updated_at)", nativeQuery = true)
        List<Object[]> getMonthlyRevenueRaw();
}

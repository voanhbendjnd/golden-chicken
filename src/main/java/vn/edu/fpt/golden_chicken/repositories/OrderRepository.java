package vn.edu.fpt.golden_chicken.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        Page<Order> findByStatusAndShipperIsNull(OrderStatus status, Pageable pageable);

        Page<Order> findByStatusAndShipperNot(OrderStatus status, Staff shipper, Pageable pageable);

        long countByShipper(Staff shipper);

        boolean existsByShipperId(Long id);

        @EntityGraph(attributePaths = { "orderItems", "orderItems.product" })
        Page<Order> findAll(Specification<Order> spec, Pageable pageable);

        @EntityGraph(attributePaths = { "orderItems", "orderItems.product" })
        java.util.Optional<Order> findWithItemsById(Long id);

        Page<Order> findByShipperAndStatusIn(Staff shipper, List<OrderStatus> statuses, Pageable pageable);

        Page<Order> findByCustomerUserIdAndStatusIn(Long userId, List<OrderStatus> statuses, Pageable pageable);

        Page<Order> findByShipperAndStatus(Staff shipper, OrderStatus status, Pageable pageable);

        long countByShipperAndStatusAndUpdatedAtBetween(Staff shipper, OrderStatus status, LocalDateTime start,
                        LocalDateTime end);

        long countByShipperAndStatus(Staff shipper, OrderStatus status);

        long countByShipperAndStatusIn(Staff shipper, List<OrderStatus> statuses);

        @Query("select coalesce(sum(o.finalAmount),0) from Order o where o.shipper = ?1 and o.paymentMethod = ?2 and o.paymentStatus = ?3")
        java.math.BigDecimal sumFinalAmountByShipperAndPaymentMethodAndPaymentStatus(Staff shipper,
                        PaymentMethod paymentMethod, PaymentStatus paymentStatus);

        @Query(value = "SELECT MONTH(o.updated_at) as month, SUM(o.final_amount) as revenue " +
                        "FROM orders o " +
                        "WHERE o.status = 'DELIVERED' " +
                        "AND YEAR(o.updated_at) = YEAR(GETDATE()) " +
                        "GROUP BY MONTH(o.updated_at) " +
                        "ORDER BY MONTH(o.updated_at)", nativeQuery = true)
        List<Object[]> getMonthlyRevenueRaw();

        @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = vn.edu.fpt.golden_chicken.utils.constants.OrderStatus.DELIVERED AND o.createdAt >= :start AND o.createdAt <= :end")
        java.math.BigDecimal getTotalRevenueBetween(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status = vn.edu.fpt.golden_chicken.utils.constants.OrderStatus.DELIVERED AND o.createdAt >= :start AND o.createdAt <= :end")
        long countSuccessfulOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query(value = "SELECT TOP 1 FORMAT(o.created_at, 'yyyy-MM-dd') as date, SUM(o.final_amount) as revenue " +
                        "FROM orders o WHERE o.status = 'DELIVERED' AND YEAR(o.created_at) = YEAR(GETDATE()) " +
                        "GROUP BY FORMAT(o.created_at, 'yyyy-MM-dd') " +
                        "ORDER BY revenue DESC", nativeQuery = true)
        List<Object[]> getHighestRevenueDay();

        @Query(value = "SELECT TOP 1 MONTH(o.created_at) as month, SUM(o.final_amount) as revenue " +
                        "FROM orders o WHERE o.status = 'DELIVERED' AND YEAR(o.created_at) = YEAR(GETDATE()) " +
                        "GROUP BY MONTH(o.created_at) " +
                        "ORDER BY revenue DESC", nativeQuery = true)
        List<Object[]> getHighestRevenueMonth();

        @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
        List<Order> findTop5RecentOrders(Pageable pageable);

        @Query(value = "SELECT FORMAT(o.created_at, 'yyyy-MM-dd') as date, SUM(o.final_amount) as revenue " +
                        "FROM orders o WHERE o.status = 'DELIVERED' AND o.created_at >= :start " +
                        "GROUP BY FORMAT(o.created_at, 'yyyy-MM-dd') " +
                        "ORDER BY date ASC", nativeQuery = true)
        List<Object[]> getDailyRevenueLast7Days(@Param("start") LocalDateTime start);
}

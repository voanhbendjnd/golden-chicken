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

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @EntityGraph(attributePaths = { "orderItems", "orderItems.product" })
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Query(value = "SELECT MONTH(o.updated_at) as month, SUM(o.final_amount) as revenue " +
            "FROM orders o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND YEAR(o.updated_at) = YEAR(GETDATE()) " +
            "GROUP BY MONTH(o.updated_at) " +
            "ORDER BY MONTH(o.updated_at)", nativeQuery = true)
    List<Object[]> getMonthlyRevenueRaw();
}

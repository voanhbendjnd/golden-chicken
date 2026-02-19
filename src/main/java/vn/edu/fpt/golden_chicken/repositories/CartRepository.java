package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.CartItem;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long>, JpaSpecificationExecutor<CartItem> {
    List<CartItem> findByCustomerId(Long id);

    List<CartItem> findByIdIn(List<Long> ids);

    CartItem findByCustomerIdAndProductId(Long customerId, Long productId);
}

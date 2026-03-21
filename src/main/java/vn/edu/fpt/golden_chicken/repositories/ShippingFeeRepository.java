package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.edu.fpt.golden_chicken.domain.entity.ShippingFee;

public interface ShippingFeeRepository extends JpaRepository<ShippingFee, Long>, JpaSpecificationExecutor<ShippingFee> {
    boolean existsByWardIgnoreCase(String ward);

    ShippingFee findByWardIgnoreCase(String ward);

    boolean existsByWardIgnoreCaseAndIdNot(String ward, Long id);
}

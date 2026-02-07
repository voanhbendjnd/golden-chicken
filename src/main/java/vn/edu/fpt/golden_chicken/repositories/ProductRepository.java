package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByIdIn(List<Long> ids);

    List<Product> findByActiveTrue();

    List<Product> findByTypeAndActiveTrue(ProductType type);

}

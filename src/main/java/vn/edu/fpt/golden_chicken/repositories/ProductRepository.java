package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByActiveTrue();

    @Query("select p from Product p where p.type = vn.edu.fpt.golden_chicken.utils.constants.ProductType.COMBO")
    List<Product> getAllComboProducts();

}

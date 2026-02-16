package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByIdIn(List<Long> ids);

    boolean existsByNameIgnoreCase(String name);

    List<Product> findByActiveTrue();

    List<Product> findByTypeAndActiveTrue(ProductType type);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select p from Product p join fetch p.category where p.category.name = :categoryName")
    List<Product> findTop5ByCategoryNameRandom(@Param("categoryName") String name);

    @Query(value = "select top 5 p.* from Products p join Categories c on p.category_id = c.id where c.name = :categoryName and p.active = 1 and p.id <> :currId ORDER BY NEWID()", nativeQuery = true)
    List<Product> findRelatedProducts(@Param("categoryName") String name, @Param("currId") Long id);
}

package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.ai.ProductSuggest;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrueAndCategory_StatusTrue(String name);

    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE p.active = true AND c.status = true")
    List<Product> findByActiveTrueAndCategoryStatusTrue();

    List<Product> findByIdIn(List<Long> ids);

    boolean existsByCategoryId(Long id);

    boolean existsByNameIgnoreCase(String name);

    List<Product> findByActiveTrue();

    List<Product> findByTypeAndActiveTrue(ProductType type);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select p from Product p join fetch p.category where p.category.name = :categoryName")
    List<Product> findTop5ByCategoryNameRandom(@Param("categoryName") String name);

    @Query(value = "select top 5 p.* from Products p join Categories c on p.category_id = c.id where c.name = :categoryName and p.active = 1 and p.is_delete = 0 and p.id <> :currId ORDER BY NEWID()", nativeQuery = true)
    List<Product> findRelatedProducts(@Param("categoryName") String name, @Param("currId") Long id);

    @Query(value = "select top 5 p.* from Products p where p.active = 1 and p.id <> :currentId and p.is_delete = 0 order by p.sold desc", nativeQuery = true)
    List<Product> findByTopSold(@Param("currentId") Long id);

    @Query("select p from Product p join fetch p.category where p.active = true")
    List<Product> findAllWithCategory();

    @Query("select p from Product p join fetch p.category where p.active = true and p.category.status = true")
    List<Product> fetchAllActiveAndCategoryActive();

    @Query("select count(p) from Product p join p.category where p.active = true and p.category.status = true")
    Long countTotalProductActiveAndCategoryActive();

    @Query("select p.id as id, p.name as name, p.price as price from Product p join p.category c where p.active = true and c.status = true")
    List<ProductSuggest> getIdAndNameProductForAI();
}

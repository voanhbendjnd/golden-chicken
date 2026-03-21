package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.ComboDetail;

@Repository
public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long>, JpaSpecificationExecutor<ComboDetail> {
    @Query("SELECT cd FROM ComboDetail cd JOIN FETCH cd.product WHERE cd.combo.id = :comboId")
    List<ComboDetail> findByComboId(@Param("comboId") Long id);

    @Query("select cd from ComboDetail cd join fetch cd.product where cd.product.id = :productId")
    List<ComboDetail> findByProductId(@Param("productId") Long id);

    @Modifying
    @Query("delete from ComboDetail cd where cd.combo.id = :comboId")
    void deleteByComboId(@Param("comboId") Long comboId);

    boolean existsByProductId(long id);

    boolean existsByComboId(long id);
}

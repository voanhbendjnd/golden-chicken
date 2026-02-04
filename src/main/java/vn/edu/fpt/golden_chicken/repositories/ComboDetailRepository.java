package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.golden_chicken.domain.entity.ComboDetail;

@Repository
public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long>, JpaSpecificationExecutor<ComboDetail> {
    List<ComboDetail> findByComboId(Long id);
}

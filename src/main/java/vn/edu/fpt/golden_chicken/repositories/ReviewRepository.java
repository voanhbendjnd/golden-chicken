package vn.edu.fpt.golden_chicken.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId AND r.reviewStatus = :status")
    Double getAverageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId  AND r.reviewStatus = :status")
    Integer getTotalReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status);
}

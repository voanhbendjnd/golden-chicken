package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.domain.request.ProductStatsDTO;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
        @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId AND r.reviewStatus = :status")
        Double getAverageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);

        @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId  AND r.reviewStatus = :status")
        Integer getTotalReviews(@Param("productId") Long productId, @Param("status") ReviewStatus status);

        @Query("SELECT r.product.name, COUNT(r) as reviewCount " +
                        "FROM Review r WHERE r.reviewStatus = vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus.PUBLISHED "
                        +
                        "GROUP BY r.product.id, r.product.name " +
                        "ORDER BY reviewCount DESC")
        List<Object[]> findMostReviewedProduct(Pageable pageable);

        Page<Review> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

        @Query("select r.comment from Review r")
        List<String> getAllComment();

        @Query("SELECT r.product.id AS productId, " +
                        "COUNT(r) AS totalReviews, " +
                        "COALESCE(AVG(r.rating), 0.0) AS averageRating " +
                        "FROM Review r " +
                        "WHERE r.product.id IN :productIds AND r.reviewStatus = :status " +
                        "GROUP BY r.product.id")
        List<ProductStatsDTO> getStatsForMultipleProducts(
                        @Param("productIds") Set<Long> productIds,
                        @Param("status") ReviewStatus status);
}

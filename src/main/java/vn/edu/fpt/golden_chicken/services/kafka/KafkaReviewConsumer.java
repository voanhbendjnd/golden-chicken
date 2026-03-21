package vn.edu.fpt.golden_chicken.services.kafka;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;
import vn.edu.fpt.golden_chicken.utils.BadWordFilterUtility;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class KafkaReviewConsumer {
    ReviewRepository reviewRepository;
    BadWordFilterUtility badWordFilterUtility;
    ProductRepository productRepository;

    @KafkaListener(topics = "scan-old-reviews-topic", groupId = "reload-reviews")
    public void listenReloadAllReviews(String word) {
        int pageNumber = 0;
        int pageSize = 100;
        Page<Review> reviewPage;
        do {
            reviewPage = this.reviewRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id")));
            var reviewToUpdate = new ArrayList<Review>();
            var productIdsToSync = new HashSet<Long>();
            for (var x : reviewPage.getContent()) {
                if (x.getReviewStatus() != ReviewStatus.HIDDEN
                        && this.badWordFilterUtility.containsBadWordThoroughly(x.getComment(), word)) {
                    x.setReviewStatus(ReviewStatus.HIDDEN);
                    reviewToUpdate.add(x);
                    if (x.getProduct() != null) {
                        productIdsToSync.add(x.getProduct().getId());
                    }
                }
            }
            if (!reviewToUpdate.isEmpty()) {
                this.reviewRepository.saveAll(reviewToUpdate);
                var productAffects = this.reviewRepository.getStatsForMultipleProducts(
                        productIdsToSync, ReviewStatus.PUBLISHED);
                var productsToUpdate = this.productRepository
                        .findByIdIn(productIdsToSync.stream().collect(Collectors.toList()));
                for (var p : productsToUpdate) {
                    var stat = productAffects.stream().filter(x -> x.getProductId().equals(p.getId())).findFirst()
                            .orElse(null);
                    if (stat != null) {
                        p.setTotalReviews(stat.getTotalReviews());
                        p.setAverageRating(stat.getAverageRating());
                    } else {
                        p.setTotalReviews(0);
                        p.setAverageRating(0D);
                    }
                }
                this.productRepository.saveAll(productsToUpdate);
            }

            pageNumber++;
        } while (reviewPage.hasNext());
    }
}

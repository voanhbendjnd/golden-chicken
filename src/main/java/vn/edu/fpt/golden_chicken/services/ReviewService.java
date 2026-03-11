package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Join;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.entity.Customer;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.domain.request.ReviewDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResReview;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.OrderItemRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("null")
@RequiredArgsConstructor
public class ReviewService {
    @Autowired
    @Lazy
    @NonFinal
    ReviewService self;

    ReviewRepository reviewRepository;
    UserService userService;
    OrderItemRepository orderItemRepository;
    FileService fileService;

    public void reviewOrder(ReviewDTO dto, List<MultipartFile> files, Long orderItemId)
            throws IOException, URISyntaxException, PermissionException {
        var user = this.userService.getUserInContext();
        if (user == null) {
            throw new PermissionException("You do not have permission!");
        }
        var customer = user.getCustomer();
        if (customer == null) {
            throw new PermissionException("You do not have permission!");
        }
        var mediaUrls = new ArrayList<String>();
        if (files != null && !files.isEmpty() && !files.getFirst().getOriginalFilename().isEmpty()) {
            for (var x : files) {
                if (!this.fileService.validFile(x)) {
                    throw new IOException("File Invalid!");
                }
                mediaUrls.add(this.fileService.getLastNameFile(x, DeclareConstant.reviewFolder));
            }

        }
        self.saveReview(dto, customer, mediaUrls, orderItemId);
    }

    @Transactional
    public void saveReview(ReviewDTO dto, Customer customer, List<String> mediaUrls, Long orderItemId)
            throws PermissionException {
        var orderItem = this.orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new DataInvalidException("Product not exists!"));
        var order = orderItem.getOrder();
        if (order == null) {
            throw new DataInvalidException("Order not exist!");
        }
        if (orderItem.getReview() != null || Boolean.TRUE.equals(orderItem.getIsReview())) {
            throw new DataInvalidException("Product in this order is already reviewed!");

        }
        var product = orderItem.getProduct();

        if (product == null) {
            throw new DataInvalidException("Product not exists!");
        }
        var isStatusValid = ((order.getStatus() == OrderStatus.COMPLETED)
                || (order.getStatus() == OrderStatus.DELIVERED));
        if (!isStatusValid
                || !order.getCustomer().getId().equals(customer.getId())) {
            throw new PermissionException("You do not has permission!");
        }
        var review = new Review();
        review.setComment(dto.getComment());
        review.setCustomer(customer);
        review.setMediaUrls(mediaUrls);
        review.setOrderItem(orderItem);
        review.setRating(dto.getRating());
        review.setProduct(product);
        orderItem.setIsReview(true);
        this.reviewRepository.save(review);

    }

    public ResultPaginationDTO fetchAllReviewWithProduct(Specification<Review> spec, Pageable pageable,
            Long productId) {
        Specification<Review> ps = (r, q, c) -> {
            Join<Review, Product> productJoin = r.join("product");
            var p1 = c.equal(productJoin.get("id"), productId);
            // var p2 = c.equal(r.get("reviewStatus"), ReviewStatus.PUBLISHED);
            return c.and(p1);
        };
        var res = new ResultPaginationDTO();
        var page = this.reviewRepository.findAll(Specification.where(spec).and(ps), pageable);
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var resReview = new ResReview();
            resReview.setName(this.userService.getMaskedCustomerName(x.getCustomer().getUser().getFullName()));
            resReview.setComment(x.getComment());
            resReview.setRating(x.getRating());
            resReview.setMediaUrls(x.getMediaUrls());
            resReview.setCreatedAt(x.getCreatedAt());
            resReview.setUpdatedAt(x.getUpdatedAt());
            resReview.setId(x.getId());
            return resReview;
        }).toList());
        return res;

    }

}

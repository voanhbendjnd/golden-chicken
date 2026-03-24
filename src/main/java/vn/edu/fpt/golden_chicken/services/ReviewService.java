package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import vn.edu.fpt.golden_chicken.domain.response.ReviewMessage;
import vn.edu.fpt.golden_chicken.repositories.CustomerRepository;
import vn.edu.fpt.golden_chicken.repositories.OrderItemRepository;
import vn.edu.fpt.golden_chicken.repositories.ProductRepository;
import vn.edu.fpt.golden_chicken.repositories.ReviewRepository;
import vn.edu.fpt.golden_chicken.utils.BadWordFilterUtility;
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
    ProductRepository productRepository;
    ReviewRepository reviewRepository;
    UserService userService;
    OrderItemRepository orderItemRepository;
    FileService fileService;
    BadWordFilterUtility badWordFilterUtility;
    CustomerRepository customerRepository;
    KafkaTemplate<String, ReviewMessage> kafkaReviewTemplate;
    KafkaTemplate<String, String> kafkaBanViolate;

    public String reviewOrder(ReviewDTO dto, List<MultipartFile> files, Long orderItemId)
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
        return self.saveReview(dto, customer, mediaUrls, orderItemId);
    }

    public void updateReview(ReviewDTO dto, List<MultipartFile> files) throws Exception {
        var uploadNewUrls = new ArrayList<String>();
        if (files != null && !files.isEmpty()) {
            for (var x : files) {
                if (x != null && !x.isEmpty() && x.getOriginalFilename() != null
                        && !x.getOriginalFilename().isBlank()) {
                    if (!this.fileService.validFile(x)) {
                        throw new IOException("File Invalid!");
                    }
                    uploadNewUrls.add(this.fileService.getLastNameFile(x, DeclareConstant.reviewFolder));
                }
            }
        }
        var filesToDelete = new ArrayList<String>();
        try {
            self.updateToDatabase(dto, uploadNewUrls, filesToDelete);
        } catch (Exception e) {
            for (var newFile : uploadNewUrls) {
                try {
                    this.fileService.deleteFile(DeclareConstant.reviewFolder, newFile);
                } catch (IOException ex) {
                    System.out.println("Cannot clean up newly uploaded file: " + newFile);
                }
            }
            throw e;
        }
        for (var oldFile : filesToDelete) {
            try {
                this.fileService.deleteFile(DeclareConstant.reviewFolder, oldFile);
            } catch (IOException ex) {
                System.out.println("Cannot Delete File Physic!");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateToDatabase(ReviewDTO dto, List<String> uploadedNewUrls, List<String> filesToDelete)
            throws PermissionException {
        var user = this.userService.getUserInContext();
        if (user == null || user.getCustomer() == null)
            throw new PermissionException("Unauthorized");
        var currentReview = this.reviewRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID " + dto.getId() + " not found"));
        if (!currentReview.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new PermissionException("Not your review");
        }
        var email = user.getEmail();
        boolean check = this.badWordFilterUtility.isViolating(dto.getComment());
        if (check) {
            currentReview.setReviewStatus(ReviewStatus.REJECTED);

        } else {
            currentReview.setReviewStatus(ReviewStatus.PUBLISHED);

        }

        var originalUrls = currentReview.getMediaUrls();
        var currentUrls = new ArrayList<String>(originalUrls == null ? new ArrayList<>() : originalUrls);
        var remainUrlsName = dto.getMediaUrls();
        var it = currentUrls.iterator();
        while (it.hasNext()) {
            var img = it.next();
            if (remainUrlsName == null || !remainUrlsName.contains(img)) {
                filesToDelete.add(img);
                it.remove();
            }
        }
        if (!uploadedNewUrls.isEmpty()) {
            currentUrls.addAll(uploadedNewUrls);
        }
        currentReview.setMediaUrls(currentUrls);
        currentReview.setComment(dto.getComment());
        currentReview.setRating(dto.getRating());
        currentReview.setIsUpdate(Boolean.TRUE);
        var lastReview = this.reviewRepository.save(currentReview);
        self.syncProductRating(lastReview.getProduct().getId());
        if (check) {
            // this.redisUserService.saveRecordViolateCustomer(email);
            var customer = currentReview.getCustomer();
            var record = customer.getViolationCount() != null ? customer.getViolationCount() : 0;
            customer.setViolationCount(record += 1);
            if (record > 4) {
                // this.redisUserService.lockAccountVilote(email);
                customer.setLockedUntil(LocalDateTime.now().plusMinutes(1));
                this.kafkaBanViolate.send("violate-account-topic", email);
                this.userService.forceLogoutCurrentUser();

            } else {

                var msgReview = new ReviewMessage();
                msgReview.setComment(lastReview.getComment());
                msgReview.setEmail(email);
                msgReview.setProductName(lastReview.getProduct().getName());
                msgReview.setRecord(record);
                this.kafkaReviewTemplate.send("violate-review-topic", msgReview);
            }
        }
    }

    @Transactional
    public void updateReviewText(Long id, Integer rating, String comment) throws PermissionException {
        var user = this.userService.getUserInContext();
        if (user == null || user.getCustomer() == null)
            throw new PermissionException("Unauthorized");
        var review = this.reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID" + id + " not found!"));
        if (!review.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new PermissionException("Not your review");
        }
        review.setComment(comment);
        review.setRating(rating);
        review.setIsUpdate(Boolean.TRUE);
        review.setReviewStatus(ReviewStatus.PUBLISHED);
        this.reviewRepository.save(review);
    }

    @Transactional
    public String saveReview(ReviewDTO dto, Customer customer, List<String> mediaUrls, Long orderItemId)
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
        var email = customer.getUser().getEmail();
        var review = new Review();

        review.setComment(dto.getComment());
        review.setCustomer(customer);
        review.setMediaUrls(mediaUrls);
        review.setOrderItem(orderItem);
        review.setRating(dto.getRating());
        review.setProduct(product);
        orderItem.setIsReview(true);
        boolean check = this.badWordFilterUtility.isViolating(dto.getComment());
        if (check) {
            review.setReviewStatus(ReviewStatus.REJECTED);
        }
        var lastReview = this.reviewRepository.save(review);
        if (check) {
            // this.redisUserService.saveRecordViolateCustomer(email);
            Integer record = customer.getViolationCount() != null ? customer.getViolationCount() : 0;
            customer.setViolationCount(record += 1);
            if (record > 4) {

                // this.redisUserService.lockAccountVilote(email);
                customer.setLockedUntil(LocalDateTime.now().plusMinutes(1));
                this.kafkaBanViolate.send("violate-account-topic", email);
                this.userService.forceLogoutCurrentUser();
                return "fail_" + product.getId();

            } else {

                var msgReview = new ReviewMessage();
                msgReview.setComment(lastReview.getComment());
                msgReview.setEmail(email);
                msgReview.setProductName(product.getName());
                msgReview.setRecord(record);
                this.kafkaReviewTemplate.send("violate-review-topic", msgReview);
                return "fail_" + product.getId();
            }

        }
        self.syncProductRating(product.getId());

        return "success_" + product.getId();

    }

    public ResultPaginationDTO fetchAllReviewWithProduct(Specification<Review> spec, Pageable pageable,
            Long productId) {
        var user = this.userService.getUserInContext();
        boolean isStaff = (user != null && (user.getRole().getName().equals("STAFF")
                || user.getRole().getName().equals(DeclareConstant.roleNameAdmin)));
        Specification<Review> ps = (r, q, c) -> {
            Join<Review, Product> productJoin = r.join("product");
            var p1 = c.equal(productJoin.get("id"), productId);
            if (isStaff) {
                var p2 = c.equal(r.get("reviewStatus"), ReviewStatus.PUBLISHED);
                // var p3 = c.equal(r.get("reviewStatus"), ReviewStatus.DELETED);
                var p4 = c.equal(r.get("reviewStatus"), ReviewStatus.REJECTED);
                return c.and(p1, c.or(p2, p4));
            } else {
                var customer = user.getCustomer();
                var p2 = c.equal(r.get("reviewStatus"), ReviewStatus.PUBLISHED);
                Join<Review, Customer> customerJoin = r.join("customer");
                var p3 = c.equal(customerJoin.get("id"), customer.getId());
                var p4 = c.equal(r.get("reviewStatus"), ReviewStatus.REJECTED);
                var myRejectedReviews = c.and(p3, p4);
                var visibleReviews = c.or(p2, myRejectedReviews);
                return c.and(p1, visibleReviews);
            }
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
            resReview.setReviewStatus(x.getReviewStatus());
            resReview.setId(x.getId());
            resReview.setCustomerId(x.getCustomer().getId());
            resReview.setIsUpdate(Boolean.TRUE.equals(x.getIsUpdate()));
            boolean isAllowed = LocalDateTime.now().isBefore(x.getCreatedAt().plusDays(7));
            resReview.setAllowReview(isAllowed);
            return resReview;
        }).toList());
        return res;

    }

    public void changeStatusReviewToHidden(Long id) throws PermissionException {
        var user = this.userService.getUserInContext();
        if (user == null || user.getCustomer() == null)
            throw new PermissionException("Unauthorized");
        var review = this.reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID" + id + " not found!"));
        if (!review.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new PermissionException("Not your review");
        }
        review.setReviewStatus(ReviewStatus.DELETED);
        this.reviewRepository.save(review);
        self.syncProductRating(review.getProduct().getId());
    }

    public void staffDeleteReview(Long id) throws PermissionException {
        var user = this.userService.getUserInContext();
        if (user == null || !"STAFF".equals(user.getRole().getName()))
            throw new PermissionException("Unauthorized");
        var review = this.reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID" + id + " not found!"));
        if (review.getReviewStatus() == ReviewStatus.DELETED) {
            var orderItem = review.getOrderItem();
            this.reviewRepository.delete(review);
            orderItem.setIsReview(false);
            this.orderItemRepository.save(orderItem);
        } else {
            review.setReviewStatus(ReviewStatus.DELETED);

            this.reviewRepository.save(review);
        }
        self.syncProductRating(review.getProduct().getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncProductRating(Long productId) {
        var avgRating = this.reviewRepository.getAverageRating(productId, ReviewStatus.PUBLISHED);
        var totalReview = this.reviewRepository.getTotalReviews(productId, ReviewStatus.PUBLISHED);
        var product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID" + productId + " not found!"));
        var roundeAvg = Math.round(avgRating * 10.0) / 10.0;
        product.setAverageRating(roundeAvg);
        product.setTotalReviews(totalReview);
        this.productRepository.save(product);
    }

    public ResultPaginationDTO fetchAllReviewWithPagination(Specification<Review> spec, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        var page = this.reviewRepository.findAll(spec, pageable);
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(x -> {
            var result = new ResReview();
            var customer = x.getCustomer();
            result.setComment(x.getComment());
            result.setCreatedAt(x.getCreatedAt());
            result.setCustomerId(customer.getId());
            result.setId(x.getId());
            result.setIsUpdate(x.getIsUpdate());
            result.setMediaUrls(x.getMediaUrls());
            result.setName(customer.getUser().getFullName());
            result.setOrderId(x.getOrderItem().getOrder().getId());
            var product = x.getProduct();
            result.setProductId(product.getId());
            result.setProductName(product.getName());
            result.setRating(x.getRating());
            result.setReviewStatus(x.getReviewStatus());
            return result;
        }).toList());
        return res;
    }

    public ResReview fetchReviewById(Long id) {
        var x = this.reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID (" + id + ") not found!"));
        var result = new ResReview();
        var customer = x.getCustomer();
        result.setComment(x.getComment());
        result.setCreatedAt(x.getCreatedAt());
        result.setCustomerId(customer.getId());
        result.setId(x.getId());
        result.setIsUpdate(x.getIsUpdate());
        result.setMediaUrls(x.getMediaUrls());
        result.setName(customer.getUser().getFullName());
        result.setOrderId(x.getOrderItem().getOrder().getId());
        var product = x.getProduct();
        result.setProductId(product.getId());
        result.setProductName(product.getName());
        result.setRating(x.getRating());
        result.setReviewStatus(x.getReviewStatus());

        return result;
    }

    public boolean revertReviewStatus(Long reviewId, ReviewStatus status) {
        var review = this.reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with ID (" + reviewId + ") not found!"));
        if (status == ReviewStatus.PUBLISHED || status == ReviewStatus.HIDDEN) {
            review.setReviewStatus(status);
            this.reviewRepository.save(review);
            // if (status == ReviewStatus.HIDDEN) {
            self.syncProductRating(review.getProduct().getId());
            // }
            return true;
        }
        return false;
    }

    public List<String> getSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        if (query == null || query.isBlank())
            return suggestions;

        var products = this.productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query);
        for (var p : products) {
            suggestions.add("Product: " + p.getName());
        }

        var customers = this.customerRepository.findByUserFullNameContainingIgnoreCase(query);
        for (var c : customers) {
            suggestions.add("Customer: " + c.getUser().getFullName());
        }

        return suggestions;
    }

}

package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.domain.request.ReviewDTO;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.services.ReviewService;
import vn.edu.fpt.golden_chicken.services.UserService;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductDetailController {
    ProductService productService;
    ReviewService reviewService;
    UserService userService;

    @GetMapping("/product/{id:[0-9]+}")
    public String detailPage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("product", this.productService.findById(id));
        model.addAttribute("related", this.productService.relationshipByCategory(id));
        var user = this.userService.getUserInContext();
        if (user != null) {
            model.addAttribute("currentUserRole", user.getRole().getName());
            if (user.getCustomer() != null) {
                model.addAttribute("currentCustomerId", user.getCustomer().getId());
            }
        }
        return "client/product-detail";
    }

    @GetMapping("/product/review")
    @ResponseBody
    public ResponseEntity<?> showReviewProduct(@Filter Specification<Review> spec,
            @PageableDefault(size = DeclareConstant.pageSize, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(this.reviewService.fetchAllReviewWithProduct(spec, pageable, productId));
    }

    @PostMapping("/product/review/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReviewCustomer(@PathVariable("id") Long id) {
        try {
            this.reviewService.changeStatusReviewToHidden(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/product/review/staff-delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReviewStaff(@PathVariable("id") Long id) {
        try {
            this.reviewService.staffDeleteReview(id);
            return ResponseEntity.ok("Staff deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/product/review/update-text")
    @ResponseBody
    public ResponseEntity<?> updateReviewText(
            @RequestParam("id") Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment) {
        try {
            this.reviewService.updateReviewText(id, rating, comment);
            return ResponseEntity.ok("Updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/product/review/update")
    @ResponseBody
    public ResponseEntity<?> updateReview(
            @ModelAttribute ReviewDTO reviewDTO,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            this.reviewService.updateReview(reviewDTO, files);
            return ResponseEntity.ok("Updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.turkraft.springfilter.boot.Filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DeclareConstant;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.services.ReviewService;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductDetailController {
    ProductService productService;
    ReviewService reviewService;

    @GetMapping("/product/{id:[0-9]+}")
    public String detailPage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("product", this.productService.findById(id));
        model.addAttribute("related", this.productService.relationshipByCategory(id));
        return "client/product-detail";
    }

    @GetMapping("/product/review")
    @ResponseBody
    public ResponseEntity<?> showReviewProduct(@Filter Specification<Review> spec,
            @PageableDefault(size = DeclareConstant.pageSize, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(this.reviewService.fetchAllReviewWithProduct(spec, pageable, productId));
    }
}

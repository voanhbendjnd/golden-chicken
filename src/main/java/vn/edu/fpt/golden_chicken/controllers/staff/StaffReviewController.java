package vn.edu.fpt.golden_chicken.controllers.staff;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.turkraft.springfilter.boot.Filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Review;
import vn.edu.fpt.golden_chicken.domain.response.ResReview;
import vn.edu.fpt.golden_chicken.services.ReviewService;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Controller
@RequestMapping("/staff/review")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffReviewController {
    ReviewService reviewService;

    @GetMapping
    public String tablePage(@Filter Specification<Review> spec,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        var data = this.reviewService.fetchAllReviewWithPagination(spec, pageable);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("reviews", data.getResult());
        return "staff/review/manage";
    }

    @GetMapping("/{id:[0-9]+}")
    @ResponseBody
    public ResponseEntity<ResReview> getReviewDetails(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.reviewService.fetchReviewById(id));
    }

    @PostMapping("/update/{id:[0-9]+}")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@RequestParam ReviewStatus reviewStatus,
            @PathVariable("id") Long id) {
        if (this.reviewService.revertReviewStatus(id, reviewStatus)) {
            return ResponseEntity.ok().body("Update status successfully!");
        }
        return ResponseEntity.badRequest().body("Failed to update status!");
    }

    @GetMapping("/suggestions")
    @ResponseBody
    public List<String> getSuggestions(@RequestParam("query") String query) {
        return this.reviewService.getSuggestions(query);
    }

}